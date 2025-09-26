package proj.tarotmeter.axl.core.data.model

import androidx.compose.runtime.Immutable
import kotlin.collections.iterator
import kotlin.math.abs

/**
 * Represents the scores of players in a Tarot game.
 *
 * @property scores A map associating each [Player] with their score as an [Int].
 */
@Immutable
data class Scores(val scores: Map<Player, Int>) {

  /** Gives the score for a particular player */
  fun forPlayer(player: Player): Int = scores[player] ?: 0

  companion object {

    /**
     * Aggregates multiple [Scores] objects into a single [Scores] object by summing the scores for
     * each player.
     *
     * @param scores Vararg of [Scores] to aggregate.
     * @return A new [Scores] object with summed values for each player.
     */
    fun aggregateScores(vararg scores: Scores): Scores {
      val aggregated = mutableMapOf<Player, Int>()
      for (score in scores) {
        for ((player, value) in score.scores) {
          aggregated[player] = (aggregated[player] ?: 0) + value
        }
      }
      return Scores(aggregated)
    }

    /**
     * Returns the minimum points required for the taker to win, based on the number of Oudlers.
     *
     * @param oudlers The number of Oudlers (0 to 3).
     * @return The target points required for the taker to win.
     */
    private fun getTargetForOudlers(oudlers: Int): Int =
      when (oudlers) {
        0 -> 56
        1 -> 51
        2 -> 41
        else -> 36
      }

    /**
     * Calculates the scores for a single round based on the round details and the game
     * configuration.
     *
     * @param round The [Round] for which to calculate scores.
     * @param game The [Game] context containing the players.
     * @return A [Scores] object representing the scores for the round.
     * @throws IllegalStateException if the partner is not set in a 5-player game.
     */
    fun roundScores(round: Round, game: Game): Scores {
      val n = game.players.size
      val target = getTargetForOudlers(round.oudlerCount)
      val diff = round.takerPoints - target
      val sign = if (diff >= 0) 1 else -1
      val base = 25 + abs(diff)
      val baseValue = base * round.contract.multiplier * sign
      val poigneeValue = round.poignee.value * sign
      val petitAuBoutValue = round.petitAuBout.value * round.contract.multiplier
      val value = baseValue + poigneeValue + petitAuBoutValue + round.chelem.value

      val taker = round.taker
      val partner = round.partner

      val result = mutableMapOf<Player, Int>()
      game.players.forEach { result[it] = 0 }

      when (n) {
        3 -> {
          // taker vs 2 defenders
          result[taker] = value * 2
          game.players.filter { it != taker }.forEach { result[it] = -value }
        }
        4 -> {
          // taker vs 3 defenders
          result[taker] = value * 3
          game.players.filter { it != taker }.forEach { result[it] = -value }
        }
        5 -> {
          // taker + partner vs 3 or 4 defenders (partner can be the same as taker)
          checkNotNull(round.partner) { "Partner must be set for 5-player game" }
          game.players.forEach {
            if (it != taker && it != partner) {
              result[it] = -value
            }
          }
          result[taker] = value * 2
          result[partner] = (result[partner] ?: 0) + value
        }
        else -> error("The number of players must be 3, 4, or 5. Was: $n")
      }
      return Scores(result)
    }

    /**
     * Computes the global (cumulative) scores for all rounds in a game.
     *
     * @param game The [Game] for which to compute global scores.
     * @return A [Scores] object representing the total scores for each player.
     */
    fun globalScores(game: Game): Scores {
      return aggregateScores(*game.rounds.map { roundScores(it, game) }.toTypedArray())
    }
  }
}
