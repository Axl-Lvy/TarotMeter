package proj.tarotmeter.axl.data.config

import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings

internal actual fun getPlatformSpecificConfig(): Settings = StorageSettings()
