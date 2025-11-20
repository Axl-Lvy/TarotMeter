package proj.tarotmeter.axl.core.data.model.calculated

import androidx.compose.runtime.Immutable
import proj.tarotmeter.axl.core.data.model.Game
import proj.tarotmeter.axl.core.data.model.Player
import proj.tarotmeter.axl.core.data.model.Round
import proj.tarotmeter.axl.core.data.model.calculated.Scores.Companion.roundScores

/**
 * Represents comprehensive statistics for a player in a game.
 *
 * @property player The player these stats belong to.
 * @property totalRounds Total number of rounds played.
 * @property totalScore Cumulative score across all rounds.
 * @property averageScore Average score per round.
 * @property bestScore Best single-round score.
 * @property worstScore Worst single-round score.
 * @property wins Number of rounds won (positive score).
 * @property winRate Percentage of rounds won.
 * @property cumulativeTimeline Timeline of cumulative scores by round index.
 * @property takerCount Number of times player was taker.
 * @property partnerCount Number of times player was partner.
 * @property defenderCount Number of times player was defender.
 * @property takerWinRate Win rate as taker.
 * @property partnerWinRate Win rate as partner.
 * @property defenderWinRate Win rate as defender.
 */
@Immutable
data class PlayerStats(
  val player: Player,
  val totalRounds: Int,
  val totalScore: Int,
  val averageScore: Float,
  val bestScore: Int,
  val worstScore: Int,
  val wins: Int,
  val winRate: Float,
  val cumulativeTimeline: List<ScorePoint>,
  val takerCount: Int,
  val partnerCount: Int,
  val defenderCount: Int,
  val takerWinRate: Float,
  val partnerWinRate: Float,
  val defenderWinRate: Float,
) {
  companion object {

    /**
     * Builds comprehensive statistics for all players in a game.
     *
     * @param game The game to analyze.
     * @return List of PlayerStats for each player, sorted by total score descending.
     */
    fun from(game: Game): List<PlayerStats> {
      if (game.rounds.isEmpty()) return emptyList()

      val sortedRounds = game.rounds.sortedBy { it.index }
      val accumulators = GameAccumulators(game.players)

      sortedRounds.forEach { round -> processRound(round, game, accumulators) }

      return buildPlayerStatsList(accumulators)
    }

    /** Processes a single round, updating all accumulators. */
    private fun processRound(round: Round, game: Game, accumulators: GameAccumulators) {
      val roundScores = roundScores(round, game)
      updateRoleCounts(round, game.players, accumulators)
      updatePlayerScores(round, roundScores, accumulators)
    }

    /** Updates role counts for a round. */
    private fun updateRoleCounts(
      round: Round,
      players: List<Player>,
      accumulators: GameAccumulators,
    ) {
      accumulators.incrementTakerCount(round.taker)
      round.partner?.let { accumulators.incrementPartnerCount(it) }
      players
        .filter { it != round.taker && it != round.partner }
        .forEach { accumulators.incrementDefenderCount(it) }
    }

    /** Updates player scores and timelines for a round. */
    private fun updatePlayerScores(
      round: Round,
      roundScores: Scores,
      accumulators: GameAccumulators,
    ) {
      roundScores.scores.forEach { (player, score) ->
        accumulators.addScore(player, score)
        val cumulativeScore = accumulators.updateCumulativeScore(player, score)
        accumulators.addTimelinePoint(player, ScorePoint(round.index, cumulativeScore))
        updateWinsIfPositive(round, player, score, accumulators)
      }
    }

    /** Updates win counts if score is positive. */
    private fun updateWinsIfPositive(
      round: Round,
      player: Player,
      score: Int,
      accumulators: GameAccumulators,
    ) {
      if (score <= 0) return

      accumulators.incrementWins(player)
      when (getPlayerRole(round, player)) {
        PlayerRole.TAKER -> accumulators.incrementTakerWins(player)
        PlayerRole.PARTNER -> accumulators.incrementPartnerWins(player)
        PlayerRole.DEFENDER -> accumulators.incrementDefenderWins(player)
      }
    }

    /** Builds the final list of PlayerStats from accumulators. */
    private fun buildPlayerStatsList(accumulators: GameAccumulators): List<PlayerStats> {
      return accumulators.scoreHistory.keys
        .map {
          val scores: List<Int> =
            accumulators.scoreHistory[it] ?: error("Scores not found for player $it")
          buildPlayerStats(it, scores, accumulators)
        }
        .sortedBy { -it.totalScore }
    }

    /** Builds PlayerStats for a single player. */
    private fun buildPlayerStats(
      player: Player,
      scores: List<Int>,
      accumulators: GameAccumulators,
    ): PlayerStats {
      val totalRounds = scores.size
      val totalScore = scores.sum()
      val roleCounts = accumulators.getRoleCounts(player)
      val roleWinRates = accumulators.calculateRoleWinRates(player, roleCounts)

      return PlayerStats(
        player = player,
        totalRounds = totalRounds,
        totalScore = totalScore,
        averageScore = scores.average().toFloat(),
        bestScore = scores.maxOrNull() ?: 0,
        worstScore = scores.minOrNull() ?: 0,
        wins = accumulators.getWins(player),
        winRate = calculateWinRate(accumulators.getWins(player), totalRounds),
        cumulativeTimeline = accumulators.getTimeline(player),
        takerCount = roleCounts.taker,
        partnerCount = roleCounts.partner,
        defenderCount = roleCounts.defender,
        takerWinRate = roleWinRates.taker,
        partnerWinRate = roleWinRates.partner,
        defenderWinRate = roleWinRates.defender,
      )
    }

    /** Calculates win rate percentage. */
    private fun calculateWinRate(wins: Int, totalRounds: Int): Float {
      return (wins * 100f) / totalRounds
    }

    /** Gets the role of a player in a round. */
    private fun getPlayerRole(round: Round, player: Player): PlayerRole {
      return when (player) {
        round.taker -> PlayerRole.TAKER
        round.partner -> PlayerRole.PARTNER
        else -> PlayerRole.DEFENDER
      }
    }
  }
}

/** Encapsulates all mutable state needed to accumulate game statistics. */
private class GameAccumulators(players: List<Player>) {
  val scoreHistory = mutableMapOf<Player, MutableList<Int>>()
  private val timelines = mutableMapOf<Player, MutableList<ScorePoint>>()
  private val wins = mutableMapOf<Player, Int>()
  private val takerCounts = mutableMapOf<Player, Int>()
  private val partnerCounts = mutableMapOf<Player, Int>()
  private val defenderCounts = mutableMapOf<Player, Int>()
  private val takerWins = mutableMapOf<Player, Int>()
  private val partnerWins = mutableMapOf<Player, Int>()
  private val defenderWins = mutableMapOf<Player, Int>()
  private val cumulativeScores = players.associateWith { 0 }.toMutableMap()

  fun addScore(player: Player, score: Int) {
    scoreHistory.getOrPut(player) { mutableListOf() }.add(score)
  }

  fun updateCumulativeScore(player: Player, score: Int): Int {
    val playerCumulativeScore = cumulativeScores[player]
    requireNotNull(playerCumulativeScore) { "Cumulative score for player $player not found." }
    val updated = playerCumulativeScore + score
    cumulativeScores[player] = updated
    return updated
  }

  fun addTimelinePoint(player: Player, point: ScorePoint) {
    timelines.getOrPut(player) { mutableListOf() }.add(point)
  }

  fun incrementWins(player: Player) {
    wins[player] = (wins[player] ?: 0) + 1
  }

  fun incrementTakerCount(player: Player) {
    takerCounts[player] = (takerCounts[player] ?: 0) + 1
  }

  fun incrementPartnerCount(player: Player) {
    partnerCounts[player] = (partnerCounts[player] ?: 0) + 1
  }

  fun incrementDefenderCount(player: Player) {
    defenderCounts[player] = (defenderCounts[player] ?: 0) + 1
  }

  fun incrementTakerWins(player: Player) {
    takerWins[player] = (takerWins[player] ?: 0) + 1
  }

  fun incrementPartnerWins(player: Player) {
    partnerWins[player] = (partnerWins[player] ?: 0) + 1
  }

  fun incrementDefenderWins(player: Player) {
    defenderWins[player] = (defenderWins[player] ?: 0) + 1
  }

  fun getWins(player: Player): Int = wins[player] ?: 0

  fun getTimeline(player: Player): List<ScorePoint> = timelines[player].orEmpty()

  fun getRoleCounts(player: Player): RoleCounts {
    return RoleCounts(
      taker = takerCounts[player] ?: 0,
      partner = partnerCounts[player] ?: 0,
      defender = defenderCounts[player] ?: 0,
    )
  }

  fun calculateRoleWinRates(player: Player, roleCounts: RoleCounts): RoleWinRates {
    return RoleWinRates(
      taker = calculateRoleWinRate(takerWins[player] ?: 0, roleCounts.taker),
      partner = calculateRoleWinRate(partnerWins[player] ?: 0, roleCounts.partner),
      defender = calculateRoleWinRate(defenderWins[player] ?: 0, roleCounts.defender),
    )
  }

  private fun calculateRoleWinRate(wins: Int, count: Int): Float {
    return if (count > 0) wins * 100f / count else 0f
  }
}

/** Data holder for role counts. */
private data class RoleCounts(val taker: Int, val partner: Int, val defender: Int)

/** Data holder for role win rates. */
private data class RoleWinRates(val taker: Float, val partner: Float, val defender: Float)

private enum class PlayerRole {
  TAKER,
  PARTNER,
  DEFENDER,
}
