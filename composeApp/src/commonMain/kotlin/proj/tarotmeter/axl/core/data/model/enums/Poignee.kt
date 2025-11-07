package proj.tarotmeter.axl.core.data.model.enums

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tarotmeter.composeapp.generated.resources.Res
import tarotmeter.composeapp.generated.resources.tarot_poignee_double
import tarotmeter.composeapp.generated.resources.tarot_poignee_double_2
import tarotmeter.composeapp.generated.resources.tarot_poignee_none
import tarotmeter.composeapp.generated.resources.tarot_poignee_simple
import tarotmeter.composeapp.generated.resources.tarot_poignee_simple_2
import tarotmeter.composeapp.generated.resources.tarot_poignee_simple_double
import tarotmeter.composeapp.generated.resources.tarot_poignee_triple

/**
 * Represents the possible Poignée (handful) announcements in a Tarot game.
 *
 * @property value The bonus or value associated with the Poignée type.
 */
enum class Poignee(val value: Int, private val stringResource: StringResource) {
  /** No Poignée announced. */
  NONE(0, Res.string.tarot_poignee_none),
  /** Simple Poignée (single handful). */
  SIMPLE(20, Res.string.tarot_poignee_simple),
  /** Simple Poignée with 2 players (variant). */
  SIMPLE_2(40, Res.string.tarot_poignee_simple_2),
  /** Simple Poignée and Double Poignée together (variant). */
  SIMPLE_DOUBLE(50, Res.string.tarot_poignee_simple_double),
  /** Double Poignée (double handful). */
  DOUBLE(30, Res.string.tarot_poignee_double),
  /** Double Poignée with 2 players (variant). */
  DOUBLE_2(60, Res.string.tarot_poignee_double_2),
  /** Triple Poignée (triple handful). */
  TRIPLE(40, Res.string.tarot_poignee_triple);

  @Composable
  fun getDisplayName(): String {
    return stringResource(stringResource)
  }
}
