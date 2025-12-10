package fr.axllvy.tarotmeter.core.data.cloud.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import fr.axllvy.tarotmeter.core.data.config.AUTH_ACCESS_TOKEN
import fr.axllvy.tarotmeter.core.data.config.AUTH_REFRESH_TOKEN
import fr.axllvy.tarotmeter.core.data.config.KEEP_LOGGED_IN
import fr.axllvy.tarotmeter.util.InitializableKoinComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.inject

/**
 * A singleton class for managing Supabase authentication
 *
 * @property supabaseClient
 * @constructor Create empty Supabase auth manager
 */
class AuthManager() : InitializableKoinComponent() {

  private val supabaseClient: SupabaseClient by inject()

  private val authListeningScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

  init {
    authListeningScope.launch {
      supabaseClient.auth.sessionStatus.collect {
        when (it) {
          is SessionStatus.Authenticated -> {
            refreshUser()
          }
          is SessionStatus.NotAuthenticated -> {
            if (it.isSignOut) {
              refreshUser()
            }
          }
          else -> {
            // No action needed for other states
          }
        }
      }
    }
    if (KEEP_LOGGED_IN.value) {
      authListeningScope.launch { tryRestoreSession() }
    }
  }

  private suspend fun tryRestoreSession() {
    val refreshToken = AUTH_REFRESH_TOKEN.value
    if (refreshToken.isNotEmpty()) {
      try {
        supabaseClient.auth.refreshSession(refreshToken = refreshToken)
      } catch (_: AuthRestException) {
        KEEP_LOGGED_IN.reset()
        updateSavedTokens()
      }
    }
  }

  /** The authenticated user. This class ensures this state is always up to date. */
  var user by mutableStateOf(supabaseClient.auth.currentUserOrNull())
    private set

  /** Refresh [user] */
  private fun refreshUser() {
    user = supabaseClient.auth.currentUserOrNull()
    updateSavedTokens()
  }

  /**
   * Updates the stored authentication tokens based on the current session state.
   *
   * If a valid session exists and the user has chosen to stay logged in, the access and refresh
   * tokens from the session are stored. Otherwise, the authentication tokens and the "keep logged
   * in" preference are reset to their default values.
   */
  fun updateSavedTokens() {
    val session = supabaseClient.auth.currentSessionOrNull()
    if (session != null && KEEP_LOGGED_IN.value) {
      AUTH_REFRESH_TOKEN.value = session.refreshToken
      AUTH_ACCESS_TOKEN.value = session.accessToken
    } else {
      KEEP_LOGGED_IN.reset()
      AUTH_REFRESH_TOKEN.reset()
      AUTH_ACCESS_TOKEN.reset()
    }
  }

  /**
   * Sign in from email.
   *
   * @param providedEmail email
   * @param providedPassword password
   */
  suspend fun signInFromEmail(providedEmail: String, providedPassword: String) {
    supabaseClient.auth.signInWith(Email) {
      email = providedEmail
      password = providedPassword
    }
  }

  suspend fun signOut() {
    supabaseClient.auth.signOut()
  }

  fun registerListener(listener: suspend (SessionStatus) -> Unit) {
    authListeningScope.launch {
      supabaseClient.auth.sessionStatus.collect {
        refreshUser()
        listener(it)
      }
    }
  }

  fun registerAuthenticationListener(listener: suspend () -> Unit) {
    registerListener {
      if (it is SessionStatus.Authenticated) {
        listener()
      }
    }
  }

  /**
   * Sign up with email.
   *
   * @param providedEmail email
   * @param providedPassword password
   */
  suspend fun signUpWithEmail(providedEmail: String, providedPassword: String) {
    supabaseClient.auth.signUpWith(
      provider = Email,
      redirectUrl = "https://www.axl-lvy.fr/tarotmeter#confirm-email",
    ) {
      email = providedEmail
      password = providedPassword
    }
  }

  suspend fun verifyEmail(tokenHash: String) {
    supabaseClient.auth.verifyEmailOtp(OtpType.Email.EMAIL, tokenHash = tokenHash)
  }

  /**
   * Delete the current user's account.
   *
   * This method calls the external API endpoint to delete the user account using the Supabase Admin
   * SDK. After successful deletion, the user is automatically signed out.
   *
   * @throws IllegalStateException if the user is not authenticated
   * @throws Exception if the deletion fails on the server side
   */
  suspend fun deleteAccount() {
    val session =
      supabaseClient.auth.currentSessionOrNull()
        ?: throw IllegalStateException("User not authenticated")

    val response: HttpResponse =
      supabaseClient.httpClient.delete("https://www.axl-lvy.fr/api/delete-user") {
        headers { append("Authorization", "Bearer ${session.accessToken}") }
      }

    if (response.status != HttpStatusCode.OK) {
      throw Exception("Failed to delete account: ${response.status}")
    }

    // Sign out after successful deletion
    signOut()
  }
}
