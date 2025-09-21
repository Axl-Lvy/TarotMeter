package proj.tarotmeter.axl.model

import androidx.compose.runtime.Immutable
import proj.tarotmeter.axl.util.IdGenerator

@Immutable
data class Round(
  override val id: Int,
  val taker: Player,
  val partner: Player? = null,
  val contract: Contract,
  val oudlerCount: Int,
  val takerPoints: Int,
) : AutoIncrement {
  init {
    require(oudlerCount in 0..3) { "Oudler count must be between 0 and 3" }
    require(takerPoints in 0..91) { "Taker points must be between 0 and 91" }
  }

  constructor(
    taker: Player,
    contract: Contract,
    partner: Player?,
    oudlerCount: Int,
    takerPoints: Int,
  ) : this(IdGenerator.nextId(Round::class), taker, partner, contract, oudlerCount, takerPoints)
}
