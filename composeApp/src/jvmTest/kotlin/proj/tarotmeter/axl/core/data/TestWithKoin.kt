package proj.tarotmeter.axl.core.data

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlinx.coroutines.Dispatchers
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import proj.tarotmeter.axl.initKoinModules

interface TestWithKoin : KoinComponent {

  @BeforeTest
  fun setUp() {
    startKoin { modules(*initKoinModules(), initTestModule()) }
  }

  @AfterTest
  fun tearDown() {
    stopKoin()
  }
}

private fun initTestModule(): Module {
  return module { single { getTestDatabaseManager() } }
}

private fun getTestDatabaseManager(): DatabaseManager {
  return StandaloneLocalDatabaseManager(
    Room.inMemoryDatabaseBuilder<StandaloneLocalDatabase>()
      .setDriver(BundledSQLiteDriver())
      .setQueryCoroutineContext(Dispatchers.IO)
      .build()
  )
}
