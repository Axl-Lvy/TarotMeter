package proj.tarotmeter.axl.core.data.config

import com.russhwolf.settings.Settings
import kotlinx.datetime.LocalDateTime

val LAST_SYNC =
  ValueBasedConfigItem("LAST_SYNC", LocalDateTime(1998, 7, 24, 0, 0, 0, 0).toString()) {
    LocalDateTime.parse(it)
  }

internal expect fun getPlatformSpecificConfig(): Settings
