package proj.tarotmeter.axl

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import proj.tarotmeter.axl.provider.GamesProvider
import proj.tarotmeter.axl.provider.PlayersProvider

/**
 * Initializes koin modules
 *
 * @return An array of all koin modules
 */
fun initKoinModules(): Array<Module> {
  val providerModule = module {
    singleOf(::PlayersProvider)
    singleOf(::GamesProvider)
  }

  return arrayOf(providerModule)
}
