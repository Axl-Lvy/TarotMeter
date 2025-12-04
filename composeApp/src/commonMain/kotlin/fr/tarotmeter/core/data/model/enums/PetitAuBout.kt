package fr.tarotmeter.core.data.model.enums

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tarotmeter.composeapp.generated.resources.Res
import tarotmeter.composeapp.generated.resources.tarot_petit_au_bout_defense
import tarotmeter.composeapp.generated.resources.tarot_petit_au_bout_none
import tarotmeter.composeapp.generated.resources.tarot_petit_au_bout_taker

/**
 * Represents the possible Petit au Bout outcomes in a Tarot game.
 *
 * @property value The score adjustment for the Petit au Bout event.
 */
enum class PetitAuBout(override val value: Int, private val stringResource: StringResource) :
  Bonus {
  /** No Petit au Bout. */
  NONE(0, Res.string.tarot_petit_au_bout_none),
  /** Petit au Bout won by the taker. */
  TAKER(10, Res.string.tarot_petit_au_bout_taker),
  /** Petit au Bout won by the defense. */
  DEFENSE(-10, Res.string.tarot_petit_au_bout_defense);

  @Composable
  override fun getDisplayName(): String {
    return stringResource(stringResource)
  }
}
