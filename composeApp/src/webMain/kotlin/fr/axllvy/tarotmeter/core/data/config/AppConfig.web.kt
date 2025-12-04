package fr.axllvy.tarotmeter.core.data.config

import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings

internal actual fun getPlatformSpecificConfig(): Settings = StorageSettings()
