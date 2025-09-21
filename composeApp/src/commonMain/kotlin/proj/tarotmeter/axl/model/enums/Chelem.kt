package proj.tarotmeter.axl.model.enums

/**
 * Represents the possible Chelem (slam) outcomes in a Tarot game.
 *
 * @property value The score adjustment for the Chelem event.
 */
enum class Chelem(val value: Int) {
  /** No Chelem. */
  NONE(0),
  /** Chelem achieved but not announced. */
  NON_ANNOUNCED(200),
  /** Chelem achieved and announced. */
  ANNOUNCED(400),
  /** Chelem failed after being announced. */
  FAILED(-200),
}
