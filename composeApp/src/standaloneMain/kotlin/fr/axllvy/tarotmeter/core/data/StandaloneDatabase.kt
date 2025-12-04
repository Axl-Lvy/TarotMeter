package fr.axllvy.tarotmeter.core.data

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import fr.axllvy.tarotmeter.core.data.converter.DateConverter
import fr.axllvy.tarotmeter.core.data.converter.UuidConverter
import fr.axllvy.tarotmeter.core.data.entity.DeviceIdEntity
import fr.axllvy.tarotmeter.core.data.entity.GameEntity
import fr.axllvy.tarotmeter.core.data.entity.GamePlayerCrossRef
import fr.axllvy.tarotmeter.core.data.entity.PlayerEntity
import fr.axllvy.tarotmeter.core.data.entity.RoundEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/** Room database for standalone platforms. */
@Database(
  entities =
    [
      PlayerEntity::class,
      RoundEntity::class,
      GameEntity::class,
      GamePlayerCrossRef::class,
      DeviceIdEntity::class,
    ],
  version = 1,
  autoMigrations = [],
)
@ConstructedBy(DatabaseConstructor::class)
@TypeConverters(DateConverter::class, UuidConverter::class)
abstract class StandaloneDatabase : RoomDatabase() {
  /**
   * Player data access object.
   *
   * @return PlayerDao instance.
   */
  abstract fun getPlayerDao(): PlayerDao

  /**
   * Game data access object.
   *
   * @return GameDao instance.
   */
  abstract fun getGameDao(): GameDao

  /**
   * Miscellaneous data access object.
   *
   * @return MiscDao instance.
   */
  abstract fun getMiscDao(): MiscDao
}

/** Database constructor for platform-specific initialization. */
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object DatabaseConstructor : RoomDatabaseConstructor<StandaloneDatabase> {
  override fun initialize(): StandaloneDatabase
}

/**
 * Creates a platform-specific database builder.
 *
 * @return Room database builder.
 */
internal expect fun databaseBuilder(): RoomDatabase.Builder<StandaloneDatabase>

/**
 * Creates and configures the standalone local database instance.
 *
 * @return Configured StandaloneLocalDatabase instance.
 */
internal fun getStandaloneLocalDatabase(): StandaloneDatabase {
  return databaseBuilder()
    .fallbackToDestructiveMigration(true)
    .setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(Dispatchers.IO)
    .build()
}
