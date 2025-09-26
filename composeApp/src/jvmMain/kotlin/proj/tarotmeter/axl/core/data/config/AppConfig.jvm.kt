package proj.tarotmeter.axl.core.data.config

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import java.util.prefs.Preferences

internal actual fun getPlatformSpecificConfig(): Settings =
  PreferencesSettings(Preferences.userRoot().node("proj/tarotmeter/axl"))
