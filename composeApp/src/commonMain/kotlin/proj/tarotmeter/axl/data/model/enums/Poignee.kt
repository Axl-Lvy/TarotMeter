package proj.tarotmeter.axl.data.model.enums

/**
 * Represents the possible Poignée (handful) announcements in a Tarot game.
 *
 * @property value The bonus or value associated with the Poignée type.
 */
enum class Poignee(val value: Int) {
  /** No Poignée announced. */
  NONE(0),
  /** Simple Poignée (single handful). */
  SIMPLE(20),
  /** Simple Poignée with 2 players (variant). */
  SIMPLE_2(40),
  /** Double Poignée (double handful). */
  DOUBLE(30),
  /** Double Poignée with 2 players (variant). */
  DOUBLE_2(60),
  /** Triple Poignée (triple handful). */
  TRIPLE(40),
}
