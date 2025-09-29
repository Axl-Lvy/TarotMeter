package proj.tarotmeter.axl.core.data

import androidx.compose.runtime.Immutable
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable
import proj.tarotmeter.axl.core.data.model.Player

@Serializable
@Immutable
data class PlayerLocalStorage(
  val name: String,
  val id: Uuid,
  val updatedAt: Instant,
  val isDeleted: Boolean = false,
) {
  fun toPlayer(): Player {
    return Player(name, id, updatedAt)
  }
}
