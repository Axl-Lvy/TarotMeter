package fr.axllvy.tarotmeter

import fr.axllvy.tarotmeter.core.data.DatabaseManager
import fr.axllvy.tarotmeter.core.data.LocalDatabaseManager
import fr.axllvy.tarotmeter.core.data.cloud.CloudDatabaseManager
import fr.axllvy.tarotmeter.core.data.cloud.Downloader
import fr.axllvy.tarotmeter.core.data.cloud.SharedGamesManager
import fr.axllvy.tarotmeter.core.data.cloud.Uploader
import fr.axllvy.tarotmeter.core.data.cloud.auth.AuthManager
import fr.axllvy.tarotmeter.core.data.cloud.createSupabaseClient
import fr.axllvy.tarotmeter.core.data.config.getPlatformSpecificConfig
import fr.axllvy.tarotmeter.core.data.getPlatformSpecificDatabaseManager
import fr.axllvy.tarotmeter.core.localization.localizationModule
import fr.axllvy.tarotmeter.core.provider.DataProvider
import io.github.jan.supabase.SupabaseClient
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

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
    singleOf(::SharedGamesManager)
  }

  val providerModule = module { singleOf(::DataProvider) }

  val miscModule = module { single { getPlatformSpecificConfig() } }

  return arrayOf(authModule, dataModule, providerModule, miscModule, localizationModule)
}
