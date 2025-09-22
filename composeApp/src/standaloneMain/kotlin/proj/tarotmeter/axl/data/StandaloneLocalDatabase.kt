package proj.tarotmeter.axl.data

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import proj.tarotmeter.axl.data.converter.DateConverter
import proj.tarotmeter.axl.data.entity.GameEntity
import proj.tarotmeter.axl.data.entity.GamePlayerCrossRef
import proj.tarotmeter.axl.data.entity.PlayerEntity
import proj.tarotmeter.axl.data.entity.RoundEntity

/** Room database for standalone platforms. */
@Database(
  entities =
    [PlayerEntity::class, RoundEntity::class, GameEntity::class, GamePlayerCrossRef::class],
  version = 1,
  autoMigrations = [],
)
@ConstructedBy(DatabaseConstructor::class)
@TypeConverters(DateConverter::class)
abstract class StandaloneLocalDatabase : RoomDatabase() {
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
}

/** Database constructor for platform-specific initialization. */
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object DatabaseConstructor : RoomDatabaseConstructor<StandaloneLocalDatabase> {
  override fun initialize(): StandaloneLocalDatabase
}

/**
 * Creates a platform-specific database builder.
 *
 * @return Room database builder.
 */
internal expect fun databaseBuilder(): RoomDatabase.Builder<StandaloneLocalDatabase>

/**
 * Creates and configures the standalone local database instance.
 *
 * @return Configured StandaloneLocalDatabase instance.
 */
internal fun getStandaloneLocalDatabase(): StandaloneLocalDatabase {
  return databaseBuilder()
    .setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(Dispatchers.IO)
    .build()
}
