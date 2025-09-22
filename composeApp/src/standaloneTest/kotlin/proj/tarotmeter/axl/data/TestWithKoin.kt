package proj.tarotmeter.axl.data

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
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

expect fun getTestDatabaseManager(): DatabaseManager
