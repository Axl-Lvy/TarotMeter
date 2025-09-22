package proj.tarotmeter.axl.provider

import proj.tarotmeter.axl.model.Player

/**
 * Provides access to and management of players within the application.
 *
 * @property players The list of all players, sorted by their id
 */
class PlayersProvider {
  private val playersPerId = mutableMapOf<Int, Player>()

  val players: List<Player>
    get() = playersPerId.values.sortedBy { it.id }

  /**
   * Adds a new player.
   *
   * @param name The name of the new player
   */
  fun addPlayer(name: String) {
    val newPlayer = Player(name)
    playersPerId[newPlayer.id] = newPlayer
  }

  /**
   * Renames a player
   *
   * @param id The id of the player to rename
   * @param newName The new name for the player
   */
  fun renamePlayer(id: Int, newName: String) {
    playersPerId[id]?.rename(newName)
  }

  /**
   * Removes a player
   *
   * @param id The id of the player to remove
   */
  fun removePlayer(id: Int) {
    playersPerId.remove(id)
  }
}
