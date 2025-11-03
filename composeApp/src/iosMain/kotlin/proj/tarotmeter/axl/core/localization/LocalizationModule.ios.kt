package proj.tarotmeter.axl.core.localization

import org.koin.core.module.Module
import org.koin.dsl.module

/** iOS-specific localization module. */
actual val localizationModule: Module = module { single<Localization> { Localization() } }
