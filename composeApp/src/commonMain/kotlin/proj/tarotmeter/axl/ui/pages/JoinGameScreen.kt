package proj.tarotmeter.axl.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import proj.tarotmeter.axl.core.data.cloud.SharedGamesManager
import proj.tarotmeter.axl.core.data.cloud.auth.AuthManager
import proj.tarotmeter.axl.ui.components.SignInButton
import tarotmeter.composeapp.generated.resources.Res
import tarotmeter.composeapp.generated.resources.general_cancel
import tarotmeter.composeapp.generated.resources.general_unknown_error
import tarotmeter.composeapp.generated.resources.join_game_dialog_not_authenticated
import tarotmeter.composeapp.generated.resources.join_game_error_title
import tarotmeter.composeapp.generated.resources.join_game_invalid_code
import tarotmeter.composeapp.generated.resources.join_game_joining
import tarotmeter.composeapp.generated.resources.join_game_success_message
import tarotmeter.composeapp.generated.resources.join_game_success_title

/**
 * Screen for joining a game via invitation code. Automatically attempts to join the game and
 * redirects to history on success.
 *
 * @param invitationCode The 8-digit invitation code from the QR code or deep link
 * @param onSuccess Callback when the game is successfully joined, navigates to history
 */
@Composable
fun JoinGameScreen(invitationCode: String, onSuccess: () -> Unit) {
  val sharedGamesManager = koinInject<SharedGamesManager>()
  val authManager = koinInject<AuthManager>()

  var joinState by remember { mutableStateOf<JoinState>(JoinState.Joining) }
  var showAuthAlert by remember { mutableStateOf(false) }

  LaunchedEffect(authManager.user) {
    // Check if user is authenticated
    if (authManager.user == null) {
      showAuthAlert = true
      return@LaunchedEffect
    }

    try {
      val code = invitationCode.toIntOrNull()
      if (code == null) {
        joinState = JoinState.Error(getString(Res.string.join_game_invalid_code))
        return@LaunchedEffect
      }

      sharedGamesManager.joinGame(code)
      joinState = JoinState.Success
      // Redirect to history after a short delay
      delay(1.5.seconds)
      onSuccess()
    } catch (e: Exception) {
      joinState = JoinState.Error(e.message ?: getString(Res.string.general_unknown_error))
    }
  }

  // Show authentication alert if user is not signed in
  if (showAuthAlert) {
    AlertDialog(
      onDismissRequest = { showAuthAlert = false },
      title = { Text(stringResource(Res.string.join_game_error_title)) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
          Text(stringResource(Res.string.join_game_dialog_not_authenticated))
          SignInButton()
        }
      },
      confirmButton = {
        TextButton(onClick = { showAuthAlert = false }) {
          Text(stringResource(Res.string.general_cancel))
        }
      },
    )
  }

  Column(
    modifier = Modifier.fillMaxSize().padding(24.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    JoinGameScreenContent(joinState)
  }
}

@Composable
private fun JoinGameScreenContent(joinState: JoinState) {
  when (joinState) {
    is JoinState.Joining -> {
      CircularProgressIndicator()
      Spacer(modifier = Modifier.height(16.dp))
      Text(
        text = stringResource(Res.string.join_game_joining),
        style = MaterialTheme.typography.titleMedium,
        textAlign = TextAlign.Center,
      )
    }

    is JoinState.Success -> {
      Text(
        text = stringResource(Res.string.join_game_success_title),
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center,
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = stringResource(Res.string.join_game_success_message),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
      )
    }

    is JoinState.Error -> {
      Text(
        text = stringResource(Res.string.join_game_error_title),
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.error,
        textAlign = TextAlign.Center,
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = joinState.message,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
      )
    }
  }
}

/** Represents the state of joining a game via invitation. */
private sealed interface JoinState {
  /** Game join is in progress. */
  data object Joining : JoinState

  /** Game join succeeded. */
  data object Success : JoinState

  /** Game join failed with an error message. */
  data class Error(val message: String) : JoinState
}
