package proj.tarotmeter.axl.core.data.cloud.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import proj.tarotmeter.axl.core.data.config.AUTH_ACCESS_TOKEN
import proj.tarotmeter.axl.core.data.config.AUTH_REFRESH_TOKEN
import proj.tarotmeter.axl.core.data.config.KEEP_LOGGED_IN

/**
 * A singleton class for managing Supabase authentication
 *
 * @property supabaseClient
 * @constructor Create empty Supabase auth manager
 */
class AuthManager() : KoinComponent {

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
      } catch (e: AuthRestException) {
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

  suspend fun signUpWithEmail(providedEmail: String, providedPassword: String) {
    supabaseClient.auth.signUpWith(
      provider = Email,
      redirectUrl = "https://www.axl-lvy.fr/confirm-email?app=tarotmeter",
    ) {
      email = providedEmail
      password = providedPassword
    }
  }
}
