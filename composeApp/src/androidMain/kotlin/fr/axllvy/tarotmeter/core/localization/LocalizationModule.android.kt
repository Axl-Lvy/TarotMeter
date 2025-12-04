package fr.axllvy.tarotmeter.core.localization

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/** Android-specific localization module. */
actual val localizationModule: Module = module {
  single<Localization> { Localization(context = androidContext()) }
}
