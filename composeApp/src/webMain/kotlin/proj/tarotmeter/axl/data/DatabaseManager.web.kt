package proj.tarotmeter.axl.data

actual fun getPlatformSpecificDatabaseManager(): DatabaseManager {
  return NoOpDatabaseManager
}
