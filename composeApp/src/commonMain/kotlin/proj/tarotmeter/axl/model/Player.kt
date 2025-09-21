package proj.tarotmeter.axl.model

import proj.tarotmeter.axl.util.IdGenerator

/**
 * Player class
 *
 * @property id unique player id.
 * @property name player name.
 */
data class Player(override val id: Int, private var internalName: String) : AutoIncrement {
  val name: String
    get() = internalName

  /**
   * Default constructor with auto-incremented id
   *
   * @param name Player name.
   */
  constructor(name: String) : this(IdGenerator.nextId(Player::class), name)

  /** Renames the player */
  fun rename(newName: String) {
    internalName = newName
  }
}
