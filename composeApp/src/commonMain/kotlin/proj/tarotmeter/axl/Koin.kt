package proj.tarotmeter.axl

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import proj.tarotmeter.axl.data.config.getPlatformSpecificConfig
import proj.tarotmeter.axl.data.getPlatformSpecificDatabaseManager
import proj.tarotmeter.axl.provider.GamesProvider
import proj.tarotmeter.axl.provider.PlayersProvider

/**
 * Initializes koin modules
 *
 * @return An array of all koin modules
 */
fun initKoinModules(): Array<Module> {

  val dataModule = module { single { getPlatformSpecificDatabaseManager() } }

  val providerModule = module {
    singleOf(::PlayersProvider)
    singleOf(::GamesProvider)
    single { getPlatformSpecificConfig() }
  }

  return arrayOf(dataModule, providerModule)
}
