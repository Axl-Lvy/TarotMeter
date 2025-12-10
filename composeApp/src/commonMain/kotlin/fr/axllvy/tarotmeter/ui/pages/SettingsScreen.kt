package fr.axllvy.tarotmeter.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import fr.axllvy.tarotmeter.core.data.cloud.Downloader
import fr.axllvy.tarotmeter.core.data.cloud.auth.AuthManager
import fr.axllvy.tarotmeter.core.data.config.APP_THEME_SETTING
import fr.axllvy.tarotmeter.core.data.config.LANGUAGE_SETTING
import fr.axllvy.tarotmeter.core.localization.Language
import fr.axllvy.tarotmeter.core.localization.Localization
import fr.axllvy.tarotmeter.ui.components.ButtonRow
import fr.axllvy.tarotmeter.ui.components.CustomElevatedCard
import fr.axllvy.tarotmeter.ui.components.DangerButton
import fr.axllvy.tarotmeter.ui.components.PrimaryButton
import fr.axllvy.tarotmeter.ui.components.SectionHeader
import fr.axllvy.tarotmeter.ui.components.SegmentedButtons
import fr.axllvy.tarotmeter.ui.components.SignInButton
import fr.axllvy.tarotmeter.ui.components.SignUpButton
import fr.axllvy.tarotmeter.ui.theme.AppThemeSetting
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import tarotmeter.composeapp.generated.resources.Res
import tarotmeter.composeapp.generated.resources.settings_about
import tarotmeter.composeapp.generated.resources.settings_about_description
import tarotmeter.composeapp.generated.resources.settings_account
import tarotmeter.composeapp.generated.resources.settings_account_delete
import tarotmeter.composeapp.generated.resources.settings_account_delete_cancel
import tarotmeter.composeapp.generated.resources.settings_account_delete_confirm
import tarotmeter.composeapp.generated.resources.settings_account_delete_confirmation_message
import tarotmeter.composeapp.generated.resources.settings_account_delete_confirmation_title
import tarotmeter.composeapp.generated.resources.settings_account_delete_failed
import tarotmeter.composeapp.generated.resources.settings_account_delete_success
import tarotmeter.composeapp.generated.resources.settings_account_download_data
import tarotmeter.composeapp.generated.resources.settings_account_download_failed
import tarotmeter.composeapp.generated.resources.settings_account_download_success
import tarotmeter.composeapp.generated.resources.settings_app_theme
import tarotmeter.composeapp.generated.resources.settings_app_theme_description
import tarotmeter.composeapp.generated.resources.settings_appearance
import tarotmeter.composeapp.generated.resources.settings_version

/** Screen for application settings. Allows choosing theme and toggling hints. */
@Composable
fun SettingsScreen() {
  val localization = koinInject<Localization>()
  val authManager = koinInject<AuthManager>()
  val selectedLanguage by derivedStateOf { Language.fromIso(LANGUAGE_SETTING.value) }
  val scrollState = rememberScrollState()
  val isSignedIn = authManager.user != null
  val snackbarHostState = remember { SnackbarHostState() }

  // Force recomposition when language changes
  key(LANGUAGE_SETTING.value) {
    Column(
      Modifier.fillMaxSize().verticalScroll(scrollState),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      CustomElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
          SectionHeader(stringResource(Res.string.settings_appearance))

          ButtonRow(
            stringResource(Res.string.settings_app_theme),
            stringResource(Res.string.settings_app_theme_description),
          ) {
            SegmentedButtons(
              AppThemeSetting.entries.map { stringResource(it.displayName) },
              AppThemeSetting.entries.indexOf(APP_THEME_SETTING.value),
              onSelect = { index -> APP_THEME_SETTING.value = AppThemeSetting.entries[index] },
              key = "app_theme_selector",
            )
          }

          LanguageSelection(selectedLanguage, localization)
        }
      }

      CustomElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          SectionHeader(stringResource(Res.string.settings_about))
          Text(
            stringResource(Res.string.settings_about_description),
            style = MaterialTheme.typography.bodyMedium,
          )
          Text(
            stringResource(Res.string.settings_version),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      CustomElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          SectionHeader(stringResource(Res.string.settings_account))
          SignInButton()
          if (!isSignedIn) {
            SignUpButton()
          }
          if (isSignedIn) {
            DownloadButton(snackbarHostState)
            DeleteAccountButton(snackbarHostState)
          }
        }
      }
      SnackbarHost(hostState = snackbarHostState)
    }
  }
}

@Composable
private fun DownloadButton(
  snackbarHostState: SnackbarHostState,
  downloader: Downloader = koinInject(),
) {
  var isDownloading by rememberSaveable { mutableStateOf(false) }
  val coroutineScope = rememberCoroutineScope()
  PrimaryButton(
    text = stringResource(Res.string.settings_account_download_data),
    onClick = {
      coroutineScope.launch {
        isDownloading = true
        try {
          downloader.downloadData()
          snackbarHostState.showSnackbar(
            message = getString(Res.string.settings_account_download_success)
          )
        } catch (e: Exception) {
          snackbarHostState.showSnackbar(
            message = getString(Res.string.settings_account_download_failed)
          )
          LOGGER.e("Error: ", e)
        } finally {
          isDownloading = false
        }
      }
    },
    modifier = Modifier.fillMaxWidth(),
    enabled = !isDownloading,
  )
}

/**
 * Button to delete the user's account with confirmation dialog.
 *
 * @param snackbarHostState The snackbar host state for showing messages
 * @param authManager The authentication manager for account deletion
 */
@Composable
private fun DeleteAccountButton(
  snackbarHostState: SnackbarHostState,
  authManager: AuthManager = koinInject(),
) {
  var showConfirmationDialog by rememberSaveable { mutableStateOf(false) }
  var isDeleting by rememberSaveable { mutableStateOf(false) }
  val coroutineScope = rememberCoroutineScope()

  DangerButton(
    text = stringResource(Res.string.settings_account_delete),
    onClick = { showConfirmationDialog = true },
    modifier = Modifier.fillMaxWidth(),
    enabled = !isDeleting,
  )

  if (showConfirmationDialog) {
    AlertDialog(
      onDismissRequest = { showConfirmationDialog = false },
      title = { Text(stringResource(Res.string.settings_account_delete_confirmation_title)) },
      text = { Text(stringResource(Res.string.settings_account_delete_confirmation_message)) },
      confirmButton = {
        TextButton(
          onClick = {
            showConfirmationDialog = false
            coroutineScope.launch {
              isDeleting = true
              try {
                authManager.deleteAccount()
                snackbarHostState.showSnackbar(
                  message = getString(Res.string.settings_account_delete_success)
                )
              } catch (e: Exception) {
                snackbarHostState.showSnackbar(
                  message = getString(Res.string.settings_account_delete_failed)
                )
                LOGGER.e("Error deleting account: ", e)
              } finally {
                isDeleting = false
              }
            }
          }
        ) {
          Text(
            stringResource(Res.string.settings_account_delete_confirm),
            color = MaterialTheme.colorScheme.error,
          )
        }
      },
      dismissButton = {
        TextButton(onClick = { showConfirmationDialog = false }) {
          Text(stringResource(Res.string.settings_account_delete_cancel))
        }
      },
    )
  }
}

@Composable expect fun LanguageSelection(selectedLanguage: Language, localization: Localization)

private val LOGGER = Logger.withTag("DownloadButton")
