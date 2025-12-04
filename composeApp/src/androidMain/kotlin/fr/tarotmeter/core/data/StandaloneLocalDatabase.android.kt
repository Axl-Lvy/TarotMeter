package fr.tarotmeter.core.data

import androidx.room.Room
import androidx.room.RoomDatabase
import fr.tarotmeter.MAIN_ACTIVITY

internal actual fun databaseBuilder(): RoomDatabase.Builder<StandaloneDatabase> {
  val appContext = MAIN_ACTIVITY.applicationContext
  val dbFile = appContext.getDatabasePath("tarot.db")
  return Room.databaseBuilder<StandaloneDatabase>(context = appContext, name = dbFile.absolutePath)
}
