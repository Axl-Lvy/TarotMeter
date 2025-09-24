package proj.tarotmeter.axl

import io.github.jan.supabase.SupabaseClient
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import proj.tarotmeter.axl.core.data.cloud.auth.AuthManager
import proj.tarotmeter.axl.core.data.cloud.createSupabaseClient
import proj.tarotmeter.axl.core.data.config.getPlatformSpecificConfig
import proj.tarotmeter.axl.core.data.getPlatformSpecificDatabaseManager
import proj.tarotmeter.axl.core.provider.GamesProvider
import proj.tarotmeter.axl.core.provider.PlayersProvider

/**
 * Initializes koin modules
 *
 * @return An array of all koin modules
 */
fun initKoinModules(): Array<Module> {

  val authModule = module {
    single<SupabaseClient> { createSupabaseClient() }
    singleOf(::AuthManager)
  }

  val dataModule = module { single { getPlatformSpecificDatabaseManager() } }

  val providerModule = module {
    singleOf(::PlayersProvider)
    singleOf(::GamesProvider)
    single { getPlatformSpecificConfig() }
  }

  return arrayOf(authModule, dataModule, providerModule)
}
