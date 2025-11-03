package proj.tarotmeter.axl

import io.github.jan.supabase.SupabaseClient
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import proj.tarotmeter.axl.core.data.DatabaseManager
import proj.tarotmeter.axl.core.data.LocalDatabaseManager
import proj.tarotmeter.axl.core.data.cloud.CloudDatabaseManager
import proj.tarotmeter.axl.core.data.cloud.Downloader
import proj.tarotmeter.axl.core.data.cloud.Uploader
import proj.tarotmeter.axl.core.data.cloud.auth.AuthManager
import proj.tarotmeter.axl.core.data.cloud.createSupabaseClient
import proj.tarotmeter.axl.core.data.config.getPlatformSpecificConfig
import proj.tarotmeter.axl.core.data.getPlatformSpecificDatabaseManager
import proj.tarotmeter.axl.core.localization.localizationModule
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

  val dataModule = module {
    single { getPlatformSpecificDatabaseManager() }
    single { get<DatabaseManager>() as LocalDatabaseManager }
    singleOf(::CloudDatabaseManager)
    singleOf(::Uploader)
    singleOf(::Downloader)
  }

  val providerModule = module {
    singleOf(::PlayersProvider)
    singleOf(::GamesProvider)
  }

  val miscModule = module { single { getPlatformSpecificConfig() } }

  return arrayOf(authModule, dataModule, providerModule, miscModule, localizationModule)
}
