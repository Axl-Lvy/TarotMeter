package proj.tarotmeter.axl.data.model.enums

/**
 * Represents the contract types in a Tarot game. Each contract has a different multiplier affecting
 * the score calculation.
 *
 * @property multiplier The score multiplier for this contract
 * @property title The human-readable name of the contract
 */
enum class Contract(val multiplier: Int, val title: String) {
  /** Petite contract (1x multiplier) */
  PETITE(1, "Petite"),

  /** Garde contract (2x multiplier) */
  GARDE(2, "Garde"),

  /** Garde Sans contract (4x multiplier) */
  GARDE_SANS(4, "Garde Sans"),

  /** Garde Contre contract (6x multiplier) */
  GARDE_CONTRE(6, "Garde Contre"),
}
