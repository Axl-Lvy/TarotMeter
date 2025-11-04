package proj.tarotmeter.axl.core.data.model

import androidx.compose.runtime.Immutable
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable
import proj.tarotmeter.axl.util.DateUtil

/**
 * Player class
 *
 * @property id unique player id.
 * @property name player name.
 * @property updatedAt timestamp of the last update.
 */
@Serializable
@Immutable
class Player(
  val name: String,
  val id: Uuid = Uuid.random(),
  val updatedAt: Instant = DateUtil.now(),
) {

  override fun toString(): String {
    return "Player(id=$id, name='$name')"
  }

  override fun hashCode(): Int {
    return id.hashCode()
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Player) return false
    if (id != other.id) return false
    return true
  }
}
