package proj.tarotmeter.axl.data.model

import kotlin.uuid.Uuid

/**
 * Player class
 *
 * @property id unique player id.
 * @property name player name.
 */
data class Player(private var internalName: String, val id: Uuid = Uuid.random()) {
  val name: String
    get() = internalName

  /** Renames the player */
  fun rename(newName: String) {
    internalName = newName
  }
}
