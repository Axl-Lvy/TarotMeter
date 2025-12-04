package fr.axllvy.tarotmeter.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import fr.axllvy.tarotmeter.core.data.cloud.auth.AuthManager
import fr.axllvy.tarotmeter.core.data.config.KEEP_LOGGED_IN
import fr.axllvy.tarotmeter.ui.theme.GOOD_TINT
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import tarotmeter.composeapp.generated.resources.Res
import tarotmeter.composeapp.generated.resources.general_cancel
import tarotmeter.composeapp.generated.resources.general_no
import tarotmeter.composeapp.generated.resources.general_yes
import tarotmeter.composeapp.generated.resources.settings_account_email
import tarotmeter.composeapp.generated.resources.settings_account_invalid_credentials
import tarotmeter.composeapp.generated.resources.settings_account_logged_in
import tarotmeter.composeapp.generated.resources.settings_account_login
import tarotmeter.composeapp.generated.resources.settings_account_login_failed
import tarotmeter.composeapp.generated.resources.settings_account_logout
import tarotmeter.composeapp.generated.resources.settings_account_logout_confirmation_question
import tarotmeter.composeapp.generated.resources.settings_account_password
import tarotmeter.composeapp.generated.resources.settings_account_sign_up
import tarotmeter.composeapp.generated.resources.settings_account_sign_up_failed
import tarotmeter.composeapp.generated.resources.settings_account_sign_up_success_message
import tarotmeter.composeapp.generated.resources.settings_account_sign_up_success_ok
import tarotmeter.composeapp.generated.resources.settings_account_sign_up_success_title
import tarotmeter.composeapp.generated.resources.settings_account_stayed_logged_in_question

/** A button that allows the user to sign in or sign out. */
@Composable
fun SignInButton(modifier: Modifier = Modifier, authManager: AuthManager = koinInject()) {
  var showSignInDialog by rememberSaveable { mutableStateOf(false) }
  val signOutDialog = remember { ConfirmationDialog(okText = Res.string.settings_account_logout) }
  val staySignedInDialog = remember {
    ConfirmationDialog(okText = Res.string.general_yes, Res.string.general_no)
  }
  val coroutineScope = rememberCoroutineScope()
  val isSignedIn = authManager.user != null

  signOutDialog.DrawDialog()
  var canShowStaySignedInDialog by rememberSaveable { mutableStateOf(false) }
  staySignedInDialog.DrawDialog()

  if (canShowStaySignedInDialog) {
    if (isSignedIn) {
      staySignedInDialog.show(Res.string.settings_account_stayed_logged_in_question) {
        KEEP_LOGGED_IN.value = true
        authManager.updateSavedTokens()
      }
    }
    canShowStaySignedInDialog = false
  }

  val buttonColor =
    if (isSignedIn) GOOD_TINT.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary
  Button(
    onClick = {
      if (!isSignedIn) {
        showSignInDialog = true
      } else {
        signOutDialog.show(Res.string.settings_account_logout_confirmation_question) {
          coroutineScope.launch {
            KEEP_LOGGED_IN.reset()
            authManager.signOut()
          }
        }
      }
    },
    modifier = modifier.fillMaxWidth().padding(bottom = 8.dp),
    colors =
      ButtonDefaults.buttonColors(
        containerColor = buttonColor,
        disabledContainerColor = buttonColor,
      ),
    enabled = true,
    shape = ButtonDefaults.shape,
    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
  ) {
    if (isSignedIn) {
      val icon = Icons.Filled.CheckCircle
      Icon(
        icon,
        contentDescription = icon.name,
        tint = GOOD_TINT,
        modifier = Modifier.padding(end = 8.dp),
      )
      Text(stringResource(Res.string.settings_account_logged_in), color = GOOD_TINT)
    } else {
      val icon = Icons.Filled.AccountCircle
      Icon(
        icon,
        contentDescription = icon.name,
        tint = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.padding(end = 8.dp),
      )
      Text(
        stringResource(Res.string.settings_account_login),
        color = MaterialTheme.colorScheme.onPrimary,
      )
    }
  }

  if (showSignInDialog) {
    KEEP_LOGGED_IN.reset()
    SignInDialog(
      dismiss = {
        showSignInDialog = false
        canShowStaySignedInDialog = true
      }
    )
  }
}

/**
 * Displays a dialog for user sign-in, allowing entry of email and password.
 *
 * Presents a form for the user to enter their credentials, handles authentication via
 * [authManager], and displays any sign-in errors. The dialog can be dismissed via the [dismiss]
 * callback.
 *
 * @param dismiss Called when the dialog should be closed, either by user action or after successful
 *   sign-in.
 */
@Composable
private fun SignInDialog(dismiss: () -> Unit, authManager: AuthManager = koinInject()) {
  AuthDialog(
    titleRes = Res.string.settings_account_login,
    buttonTextRes = Res.string.settings_account_login,
    errorFallbackRes = Res.string.settings_account_login_failed,
    dismiss = dismiss,
    authAction = { email, password -> authManager.signInFromEmail(email, password) },
  )
}

/**
 * Displays a dialog for user authentication, allowing entry of email and password.
 *
 * @param titleRes Resource for the dialog title.
 * @param buttonTextRes Resource for the confirm button text.
 * @param errorFallbackRes Resource for fallback error message.
 * @param dismiss Called when the dialog should be closed.
 * @param authAction Suspend function that performs the authentication action.
 * @param onSuccess Optional callback for successful authentication.
 */
@Composable
private fun AuthDialog(
  titleRes: org.jetbrains.compose.resources.StringResource,
  buttonTextRes: org.jetbrains.compose.resources.StringResource,
  errorFallbackRes: org.jetbrains.compose.resources.StringResource,
  dismiss: () -> Unit,
  authAction: suspend (String, String) -> Unit,
  onSuccess: (() -> Unit)? = null,
) {
  val coroutineScope = rememberCoroutineScope()
  var email by rememberSaveable { mutableStateOf("") }
  var password by rememberSaveable { mutableStateOf("") }
  var authError by rememberSaveable { mutableStateOf<String?>(null) }
  var isProcessing by rememberSaveable { mutableStateOf(false) }
  AlertDialog(
    onDismissRequest = { dismiss() },
    title = { Text(stringResource(titleRes)) },
    text = {
      Column {
        OutlinedTextField(
          value = email,
          onValueChange = { email = it },
          label = { Text(stringResource(Res.string.settings_account_email)) },
          modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
          value = password,
          onValueChange = { password = it },
          label = { Text(stringResource(Res.string.settings_account_password)) },
          visualTransformation = PasswordVisualTransformation(),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
          modifier = Modifier.fillMaxWidth(),
        )
        ErrorPayload(authError)
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          coroutineScope.launch {
            isProcessing = true
            try {
              authAction(email, password)
              authError = null
            } catch (e: Exception) {
              authError = e.message ?: getString(errorFallbackRes)
            }
            if (authError == null) {
              onSuccess?.invoke()
              dismiss()
            }
            isProcessing = false
          }
        },
        enabled = !isProcessing,
      ) {
        if (isProcessing) CircularProgressIndicator() else Text(stringResource(buttonTextRes))
      }
    },
    dismissButton = {
      TextButton(onClick = { dismiss() }, enabled = !isProcessing) {
        Text(stringResource(Res.string.general_cancel))
      }
    },
  )
}

@Composable
private fun ErrorPayload(signInError: String?) {
  if (signInError != null) {
    if (signInError.contains("Invalid login credentials")) {
      Text(
        stringResource(Res.string.settings_account_invalid_credentials),
        color = MaterialTheme.colorScheme.error,
      )
    } else {
      Text(signInError, color = MaterialTheme.colorScheme.error)
    }
  }
}

/** A button that allows the user to sign up for a new account. */
@Composable
fun SignUpButton(modifier: Modifier = Modifier) {
  var showSignUpDialog by rememberSaveable { mutableStateOf(false) }
  var showSuccessDialog by rememberSaveable { mutableStateOf(false) }

  Button(
    onClick = { showSignUpDialog = true },
    modifier = modifier.fillMaxWidth().padding(bottom = 8.dp),
    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
    shape = ButtonDefaults.shape,
    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
  ) {
    val icon = Icons.Filled.PersonAdd
    Icon(
      icon,
      contentDescription = icon.name,
      tint = MaterialTheme.colorScheme.onSecondary,
      modifier = Modifier.padding(end = 8.dp),
    )
    Text(
      stringResource(Res.string.settings_account_sign_up),
      color = MaterialTheme.colorScheme.onSecondary,
    )
  }

  if (showSignUpDialog) {
    SignUpDialog(
      dismiss = { showSignUpDialog = false },
      onSuccess = {
        showSignUpDialog = false
        showSuccessDialog = true
      },
    )
  }

  if (showSuccessDialog) {
    SignUpSuccessDialog(dismiss = { showSuccessDialog = false })
  }
}

/**
 * Displays a dialog for user sign-up.
 *
 * @param dismiss Called when the dialog should be closed.
 * @param onSuccess Called when sign-up is successful.
 */
@Composable
private fun SignUpDialog(
  dismiss: () -> Unit,
  onSuccess: () -> Unit,
  authManager: AuthManager = koinInject(),
) {
  AuthDialog(
    titleRes = Res.string.settings_account_sign_up,
    buttonTextRes = Res.string.settings_account_sign_up,
    errorFallbackRes = Res.string.settings_account_sign_up_failed,
    dismiss = dismiss,
    authAction = { email, password -> authManager.signUpWithEmail(email, password) },
    onSuccess = onSuccess,
  )
}

/**
 * Displays a success dialog after sign-up, informing the user to check their email.
 *
 * @param dismiss Called when the dialog should be closed.
 */
@Composable
private fun SignUpSuccessDialog(dismiss: () -> Unit) {
  AlertDialog(
    onDismissRequest = { dismiss() },
    title = { Text(stringResource(Res.string.settings_account_sign_up_success_title)) },
    text = { Text(stringResource(Res.string.settings_account_sign_up_success_message)) },
    confirmButton = {
      TextButton(onClick = { dismiss() }) {
        Text(stringResource(Res.string.settings_account_sign_up_success_ok))
      }
    },
  )
}
