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
data class Player(
  val name: String,
  val id: Uuid = Uuid.random(),
  val updatedAt: Instant = DateUtil.now(),
)
