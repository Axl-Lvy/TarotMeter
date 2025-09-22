package proj.tarotmeter.axl.provider

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import proj.tarotmeter.axl.data.DatabaseManager
import proj.tarotmeter.axl.data.model.Player

/** Provides access to and management of players within the application. */
class PlayersProvider : KoinComponent {

  private val databaseManager: DatabaseManager by inject()

  suspend fun getPlayers(): List<Player> = databaseManager.getPlayers()

  /**
   * Adds a new player.
   *
   * @param name The name of the new player
   */
  suspend fun addPlayer(name: String) {
    val newPlayer = Player(name)
    databaseManager.insertPlayer(newPlayer)
  }

  /**
   * Renames a player
   *
   * @param id The id of the player to rename
   * @param newName The new name for the player
   */
  suspend fun renamePlayer(id: Int, newName: String) {
    databaseManager.renamePlayer(id, newName)
  }

  /**
   * Removes a player
   *
   * @param id The id of the player to remove
   */
  suspend fun removePlayer(id: Int) {
    databaseManager.deletePlayer(id)
  }
}
