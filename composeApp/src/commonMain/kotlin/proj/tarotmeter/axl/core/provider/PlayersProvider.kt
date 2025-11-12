package proj.tarotmeter.axl.core.provider

import kotlin.uuid.Uuid
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import proj.tarotmeter.axl.core.data.DatabaseManager
import proj.tarotmeter.axl.core.data.model.Player

/** Provides access to and management of players within the application. */
class PlayersProvider : KoinComponent {

  private val databaseManager: DatabaseManager by inject()
}
