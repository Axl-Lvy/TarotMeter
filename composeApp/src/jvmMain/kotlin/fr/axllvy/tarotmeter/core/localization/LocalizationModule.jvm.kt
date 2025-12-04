package fr.axllvy.tarotmeter.core.localization

import org.koin.core.module.Module
import org.koin.dsl.module

/** JVM-specific localization module. */
actual val localizationModule: Module = module { single<Localization> { Localization() } }
