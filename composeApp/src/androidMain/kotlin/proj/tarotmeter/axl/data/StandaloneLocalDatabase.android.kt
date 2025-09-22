package proj.tarotmeter.axl.data

import androidx.room.Room
import androidx.room.RoomDatabase
import proj.tarotmeter.axl.MAIN_ACTIVITY

internal actual fun databaseBuilder(): RoomDatabase.Builder<StandaloneLocalDatabase> {
  val appContext = MAIN_ACTIVITY.applicationContext
  val dbFile = appContext.getDatabasePath("tarot.db")
  return Room.databaseBuilder<StandaloneLocalDatabase>(
    context = appContext,
    name = dbFile.absolutePath,
  )
}
