package fr.axllvy.tarotmeter.core.data

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

internal actual fun databaseBuilder(): RoomDatabase.Builder<StandaloneDatabase> {
  val dbFile = File("build/jvmDb/tarot.db")
  return Room.databaseBuilder<StandaloneDatabase>(name = dbFile.absolutePath)
}
