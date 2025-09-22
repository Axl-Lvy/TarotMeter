package proj.tarotmeter.axl.data.model.enums

/**
 * Represents the possible Petit au Bout outcomes in a Tarot game.
 *
 * @property value The score adjustment for the Petit au Bout event.
 */
enum class PetitAuBout(val value: Int) {
  /** No Petit au Bout. */
  NONE(0),
  /** Petit au Bout won by the taker. */
  TAKER(10),
  /** Petit au Bout won by the defense. */
  DEFENSE(-10),
}
