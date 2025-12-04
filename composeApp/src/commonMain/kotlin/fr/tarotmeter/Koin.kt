package fr.tarotmeter

import fr.tarotmeter.core.data.DatabaseManager
import fr.tarotmeter.core.data.LocalDatabaseManager
import fr.tarotmeter.core.data.cloud.CloudDatabaseManager
import fr.tarotmeter.core.data.cloud.Downloader
import fr.tarotmeter.core.data.cloud.SharedGamesManager
import fr.tarotmeter.core.data.cloud.Uploader
import fr.tarotmeter.core.data.cloud.auth.AuthManager
import fr.tarotmeter.core.data.cloud.createSupabaseClient
import fr.tarotmeter.core.data.config.getPlatformSpecificConfig
import fr.tarotmeter.core.data.getPlatformSpecificDatabaseManager
import fr.tarotmeter.core.localization.localizationModule
import fr.tarotmeter.core.provider.DataProvider
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
