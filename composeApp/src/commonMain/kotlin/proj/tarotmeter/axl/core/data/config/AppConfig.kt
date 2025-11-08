package proj.tarotmeter.axl.core.data.config

import com.russhwolf.settings.Settings
import kotlin.time.Instant
import proj.tarotmeter.axl.ui.theme.AppThemeSetting

val LAST_SYNC = ValueBasedConfigItem("LAST_SYNC", Instant.DISTANT_PAST) { Instant.parse(it) }

val APP_THEME_SETTING = EnumBasedConfigItem.from("APP_THEME", AppThemeSetting.SYSTEM)

val LANGUAGE_SETTING = StringConfigItem("LANGUAGE_SETTING", "und")

// These config items must be aligned with other applications (MemorChess for example)
val KEEP_LOGGED_IN = ValueBasedConfigItem("keepLoggedIn", false) { it.toBoolean() }

val AUTH_REFRESH_TOKEN = StringConfigItem("authRefreshToken")

val AUTH_ACCESS_TOKEN = StringConfigItem("authAccessToken")

internal expect fun getPlatformSpecificConfig(): Settings
