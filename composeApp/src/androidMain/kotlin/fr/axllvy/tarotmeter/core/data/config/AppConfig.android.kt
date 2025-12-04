package fr.axllvy.tarotmeter.core.data.config

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import fr.axllvy.tarotmeter.MAIN_ACTIVITY

internal actual fun getPlatformSpecificConfig(): Settings =
  SharedPreferencesSettings(MAIN_ACTIVITY.getPreferences(Context.MODE_PRIVATE))
