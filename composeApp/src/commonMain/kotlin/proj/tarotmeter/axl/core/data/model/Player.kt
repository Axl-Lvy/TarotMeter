package proj.tarotmeter.axl.core.data.model

import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable

/**
 * Player class
 *
 * @property id unique player id.
 * @property name player name.
 */
@Serializable
data class Player(private var internalName: String, val id: Uuid = Uuid.random()) {
  val name: String
    get() = internalName

  /** Renames the player */
  fun rename(newName: String) {
    internalName = newName
  }
}
