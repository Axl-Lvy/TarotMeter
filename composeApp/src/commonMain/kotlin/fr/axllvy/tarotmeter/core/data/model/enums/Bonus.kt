package fr.axllvy.tarotmeter.core.data.model.enums

import androidx.compose.runtime.Composable

/** A bonus type in a Tarot game. */
interface Bonus {

  /** The bonus or value associated with the enum type. */
  val value: Int

  /** Returns the display name for the enum type. */
  @Composable fun getDisplayName(): String
}
