package proj.tarotmeter.axl.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import proj.tarotmeter.axl.core.data.cloud.auth.AuthManager
import tarotmeter.composeapp.generated.resources.Res
import tarotmeter.composeapp.generated.resources.confirm_email_error_title
import tarotmeter.composeapp.generated.resources.confirm_email_success_message
import tarotmeter.composeapp.generated.resources.confirm_email_success_title
import tarotmeter.composeapp.generated.resources.confirm_email_verifying
import tarotmeter.composeapp.generated.resources.general_unknown_error

/**
 * Screen for confirming email verification. Displays a button that allows the user to verify their
 * email using the provided token hash.
 *
 * @param tokenHash The token hash received from the verification email
 */
@Composable
fun ConfirmEmailScreen(tokenHash: String) {
  val authManager = koinInject<AuthManager>()

  var verificationState by remember {
    mutableStateOf<VerificationState>(VerificationState.Verifying)
  }

  LaunchedEffect(Unit) {
    try {
      authManager.verifyEmail(tokenHash)
      verificationState = VerificationState.Success
    } catch (e: Exception) {
      verificationState =
        VerificationState.Error(e.message ?: getString(Res.string.general_unknown_error))
    }
  }

  Column(
    modifier = Modifier.fillMaxSize().padding(24.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    when (val currentState = verificationState) {
      is VerificationState.Verifying -> {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = stringResource(Res.string.confirm_email_verifying),
          style = MaterialTheme.typography.titleMedium,
          textAlign = TextAlign.Center,
        )
      }
      is VerificationState.Success -> {
        Text(
          text = stringResource(Res.string.confirm_email_success_title),
          style = MaterialTheme.typography.headlineSmall,
          color = MaterialTheme.colorScheme.primary,
          textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = stringResource(Res.string.confirm_email_success_message),
          style = MaterialTheme.typography.bodyLarge,
          textAlign = TextAlign.Center,
        )
      }
      is VerificationState.Error -> {
        Text(
          text = stringResource(Res.string.confirm_email_error_title),
          style = MaterialTheme.typography.headlineSmall,
          color = MaterialTheme.colorScheme.error,
          textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = currentState.message,
          style = MaterialTheme.typography.bodyLarge,
          textAlign = TextAlign.Center,
        )
      }
    }
  }
}

/** Represents the state of email verification. */
private sealed interface VerificationState {
  /** Email verification is in progress. */
  data object Verifying : VerificationState

  /** Email verification succeeded. */
  data object Success : VerificationState

  /** Email verification failed with an error message. */
  data class Error(val message: String) : VerificationState
}
