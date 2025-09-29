package proj.tarotmeter.axl.core.data.config

import com.russhwolf.settings.Settings
import kotlin.time.Instant

val LAST_SYNC = ValueBasedConfigItem("LAST_SYNC", Instant.DISTANT_PAST) { Instant.parse(it) }

val KEEP_LOGGED_IN = ValueBasedConfigItem("KEEP_LOGGED_IN", false) { it.toBoolean() }

val AUTH_REFRESH_TOKEN = StringConfigItem("AUTH_REFRESH_TOKEN")

val AUTH_ACCESS_TOKEN = StringConfigItem("AUTH_ACCESS_TOKEN")

internal expect fun getPlatformSpecificConfig(): Settings
