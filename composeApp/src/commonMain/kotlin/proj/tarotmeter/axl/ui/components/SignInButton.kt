package proj.tarotmeter.axl.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
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
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import proj.tarotmeter.axl.core.data.cloud.auth.AuthManager
import proj.tarotmeter.axl.core.data.config.KEEP_LOGGED_IN
import proj.tarotmeter.axl.ui.theme.GOOD_TINT
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
    if (authManager.user != null) {
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
  val coroutineScope = rememberCoroutineScope()
  var email by rememberSaveable { mutableStateOf("") }
  var password by rememberSaveable { mutableStateOf("") }
  var signInError by rememberSaveable { mutableStateOf<String?>(null) }
  var isSigningIn by rememberSaveable { mutableStateOf(false) }
  AlertDialog(
    onDismissRequest = { dismiss() },
    title = { Text(stringResource(Res.string.settings_account_login)) },
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
        ErrorPayload(signInError)
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          coroutineScope.launch {
            isSigningIn = true
            try {
              authManager.signInFromEmail(email, password)
              signInError = null
            } catch (e: Exception) {
              signInError = e.message ?: getString(Res.string.settings_account_login_failed)
            }
            if (signInError == null) {
              dismiss()
            }
            isSigningIn = false
          }
        },
        enabled = !isSigningIn,
      ) {
        if (isSigningIn) CircularProgressIndicator()
        else Text(stringResource(Res.string.settings_account_login))
      }
    },
    dismissButton = {
      TextButton(onClick = { dismiss() }, enabled = !isSigningIn) {
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
