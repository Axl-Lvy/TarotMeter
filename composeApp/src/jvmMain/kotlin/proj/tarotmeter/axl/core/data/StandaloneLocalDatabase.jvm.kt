package proj.tarotmeter.axl.core.data

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

internal actual fun databaseBuilder(): RoomDatabase.Builder<StandaloneLocalDatabase> {
  val dbFile = File("build/jvmDb/tarot.db")
  return Room.databaseBuilder<StandaloneLocalDatabase>(name = dbFile.absolutePath)
}
