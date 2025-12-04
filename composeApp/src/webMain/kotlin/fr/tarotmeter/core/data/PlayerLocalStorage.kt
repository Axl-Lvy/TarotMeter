package fr.tarotmeter.core.data

import androidx.compose.runtime.Immutable
import fr.tarotmeter.core.data.model.Player
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable

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
