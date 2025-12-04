package fr.tarotmeter.core.data.model.enums

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tarotmeter.composeapp.generated.resources.Res
import tarotmeter.composeapp.generated.resources.tarot_chelem_announced
import tarotmeter.composeapp.generated.resources.tarot_chelem_failed
import tarotmeter.composeapp.generated.resources.tarot_chelem_none
import tarotmeter.composeapp.generated.resources.tarot_chelem_not_announced

/**
 * Represents the possible Chelem (slam) outcomes in a Tarot game.
 *
 * @property value The score adjustment for the Chelem event.
 */
enum class Chelem(override val value: Int, private val stringResource: StringResource) : Bonus {
  /** No Chelem. */
  NONE(0, Res.string.tarot_chelem_none),
  /** Chelem achieved but not announced. */
  NOT_ANNOUNCED(200, Res.string.tarot_chelem_not_announced),
  /** Chelem achieved and announced. */
  ANNOUNCED(400, Res.string.tarot_chelem_announced),
  /** Chelem failed after being announced. */
  FAILED(-200, Res.string.tarot_chelem_failed);

  @Composable
  override fun getDisplayName(): String {
    return stringResource(stringResource)
  }
}
