package proj.tarotmeter.axl.ui.pages.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.time.Instant
import org.jetbrains.compose.resources.stringResource
import proj.tarotmeter.axl.core.data.model.Game
import proj.tarotmeter.axl.core.data.model.Player
import proj.tarotmeter.axl.core.data.model.Scores
import proj.tarotmeter.axl.ui.components.PlayerAvatar
import tarotmeter.composeapp.generated.resources.Res
import tarotmeter.composeapp.generated.resources.game_stats_chart_empty
import tarotmeter.composeapp.generated.resources.game_stats_chart_title
import tarotmeter.composeapp.generated.resources.game_stats_empty_state_message
import tarotmeter.composeapp.generated.resources.game_stats_empty_state_title
import tarotmeter.composeapp.generated.resources.game_stats_metric_avg_place
import tarotmeter.composeapp.generated.resources.game_stats_metric_avg_score
import tarotmeter.composeapp.generated.resources.game_stats_metric_best_score
import tarotmeter.composeapp.generated.resources.game_stats_metric_total_rounds
import tarotmeter.composeapp.generated.resources.game_stats_metric_win_rate
import tarotmeter.composeapp.generated.resources.game_stats_metric_worst_score
import tarotmeter.composeapp.generated.resources.game_stats_placeholder_notice

/**
 * Displays game statistics for players in a specific game.
 *
 * Shows score evolution over rounds, total score, and various metrics for each player.
 *
 * @param playerStats List of player statistics to display
 * @param modifier Modifier for the component
 * @param placeholderData Optional placeholder data to display when no real stats are available
 */
@Composable
fun GameStatsView(
  playerStats: List<PlayerStats>,
  modifier: Modifier = Modifier,
  placeholderData: List<PlayerStats>? = null,
) {
  val isUsingSampleData = playerStats.isEmpty() && !placeholderData.isNullOrEmpty()
  val statsToDisplay =
    if (playerStats.isNotEmpty()) {
      playerStats
    } else {
      placeholderData.orEmpty()
    }

  if (statsToDisplay.isEmpty()) {
    StatsEmptyState(modifier)
    return
  }

  val palette =
    listOf(
      MaterialTheme.colorScheme.primary,
      MaterialTheme.colorScheme.tertiary,
      MaterialTheme.colorScheme.secondary,
      MaterialTheme.colorScheme.error,
      MaterialTheme.colorScheme.secondaryContainer,
      MaterialTheme.colorScheme.outline,
    )
  val playerColors =
    remember(statsToDisplay, palette) {
      statsToDisplay
        .mapIndexed { index, stats -> stats.player to palette[index % palette.size] }
        .toMap()
    }

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(bottom = 32.dp),
  ) {
    if (isUsingSampleData) {
      item { SampleDataBanner() }
    }
    item {
      PlayerScoreChart(
        stats = statsToDisplay,
        playerColors = playerColors,
        modifier = Modifier.fillMaxWidth(),
      )
    }
    items(statsToDisplay, key = { it.player.id }) { stats ->
      PlayerStatsCard(
        playerStats = stats,
        accentColor = playerColors[stats.player] ?: MaterialTheme.colorScheme.primary,
      )
    }
  }
}

@Composable
private fun SampleDataBanner() {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    color = MaterialTheme.colorScheme.secondaryContainer,
    shape = MaterialTheme.shapes.medium,
    tonalElevation = 2.dp,
  ) {
    Text(
      text = stringResource(Res.string.game_stats_placeholder_notice),
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSecondaryContainer,
    )
  }
}

@Composable
private fun StatsEmptyState(modifier: Modifier = Modifier) {
  Column(
    modifier = modifier.fillMaxSize(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = stringResource(Res.string.game_stats_empty_state_title),
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.SemiBold,
    )
    Spacer(Modifier.height(8.dp))
    Text(
      text = stringResource(Res.string.game_stats_empty_state_message),
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun PlayerScoreChart(
  stats: List<PlayerStats>,
  playerColors: Map<Player, Color>,
  modifier: Modifier = Modifier,
) {
  val timelines = stats.filter { it.cumulativeTimeline.isNotEmpty() }
  if (timelines.isEmpty()) {
    Surface(
      modifier = modifier,
      shape = MaterialTheme.shapes.medium,
      tonalElevation = 2.dp,
    ) {
      Text(
        text = stringResource(Res.string.game_stats_chart_empty),
        modifier = Modifier.padding(16.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
    return
  }

  val allPoints = timelines.flatMap { it.cumulativeTimeline }
  val minX = allPoints.minOf { it.epochMillis }.toFloat()
  val maxX = allPoints.maxOf { it.epochMillis }.toFloat()
  val minY = min(0f, allPoints.minOf { it.value }.toFloat())
  val maxY = max(0f, allPoints.maxOf { it.value }.toFloat())
  val xRange = (maxX - minX).takeUnless { it == 0f } ?: 1f
  val yRange = (maxY - minY).takeUnless { it == 0f } ?: 1f

  val axisColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
  val defaultLineColor = MaterialTheme.colorScheme.primary

  Surface(
    modifier = modifier,
    shape = MaterialTheme.shapes.medium,
    tonalElevation = 2.dp,
  ) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text(
        text = stringResource(Res.string.game_stats_chart_title),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
      )
      Canvas(modifier = Modifier.fillMaxWidth().height(220.dp)) {
        val padding = 24.dp.toPx()
        val chartWidth = size.width - (padding * 2)
        val chartHeight = size.height - (padding * 2)
        val origin = Offset(padding, size.height - padding)
        drawLine(
          color = axisColor,
          start = origin,
          end = Offset(padding, padding),
          strokeWidth = 2f,
        )
        drawLine(
          color = axisColor,
          start = origin,
          end = Offset(size.width - padding / 2, size.height - padding),
          strokeWidth = 2f,
        )

        timelines.forEach { playerStat ->
          val color = playerColors[playerStat.player] ?: defaultLineColor
          val path = Path()
          playerStat.cumulativeTimeline.forEachIndexed { index, point ->
            val xRatio = (point.epochMillis - minX) / xRange
            val yRatio = (point.value - minY) / yRange
            val x = padding + (xRatio * chartWidth)
            val y = (size.height - padding) - (yRatio * chartHeight)
            if (index == 0) {
              path.moveTo(x, y)
            } else {
              path.lineTo(x, y)
            }
          }
          drawPath(path = path, color = color, style = Stroke(width = 4f))
          playerStat.cumulativeTimeline.forEach { point ->
            val xRatio = (point.epochMillis - minX) / xRange
            val yRatio = (point.value - minY) / yRange
            val x = padding + (xRatio * chartWidth)
            val y = (size.height - padding) - (yRatio * chartHeight)
            drawCircle(
              color = color,
              radius = 5.dp.toPx(),
              center = Offset(x, y),
            )
          }
        }
      }

      timelines.forEach { playerStat ->
        val color = playerColors[playerStat.player] ?: defaultLineColor
        LegendItem(
          label = playerStat.player.name,
          color = color,
        )
      }
    }
  }
}

@Composable
private fun LegendItem(label: String, color: Color) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Box(
      modifier = Modifier.size(12.dp),
      contentAlignment = Alignment.Center,
    ) { Surface(color = color, shape = MaterialTheme.shapes.small) { Spacer(Modifier.size(12.dp)) } }
    Text(text = label, style = MaterialTheme.typography.bodySmall)
  }
}

@Composable
private fun PlayerStatsCard(playerStats: PlayerStats, accentColor: Color) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.medium,
    tonalElevation = 1.dp,
  ) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        PlayerAvatar(name = playerStats.player.name)
        Column {
          Text(
            text = playerStats.player.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
          )
          Text(
            text = stringResource(Res.string.game_stats_metric_total_rounds) +
              ": ${playerStats.totalRounds}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      Text(
        text = "${playerStats.totalScore} pts",
        style = MaterialTheme.typography.titleMedium,
        color = accentColor,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.End,
      )

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        StatsMetricChip(
          label = stringResource(Res.string.game_stats_metric_avg_score),
          value = playerStats.averageScore.formatScore(),
        )
        StatsMetricChip(
          label = stringResource(Res.string.game_stats_metric_best_score),
          value = playerStats.bestScore.toString(),
        )
        StatsMetricChip(
          label = stringResource(Res.string.game_stats_metric_worst_score),
          value = playerStats.worstScore.toString(),
        )
        StatsMetricChip(
          label = stringResource(Res.string.game_stats_metric_win_rate),
          value = "${playerStats.winRate.roundToInt()}%",
        )
        StatsMetricChip(
          label = stringResource(Res.string.game_stats_metric_avg_place),
          value = playerStats.averagePlacement.formatScore(),
        )
      }
    }
  }
}

@Composable
private fun StatsMetricChip(label: String, value: String, modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    color = MaterialTheme.colorScheme.surfaceVariant,
    shape = MaterialTheme.shapes.small,
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Text(
        text = value,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
      )
    }
  }
}

@Immutable
private data class PlayerStats(
  val player: Player,
  val totalRounds: Int,
  val totalScore: Int,
  val averageScore: Float,
  val bestScore: Int,
  val worstScore: Int,
  val wins: Int,
  val winRate: Float,
  val averagePlacement: Float,
  val cumulativeTimeline: List<ScorePoint>,
)

@Immutable
data class ScorePoint(
  val date: Instant,
  val value: Int,
  val epochMillis: Long = date.toEpochMilliseconds(),
)

fun buildPlayerStats(game: Game): List<PlayerStats> {
  if (game.rounds.isEmpty()) return emptyList()
  
  val sortedRounds = game.rounds.sortedBy { it.createdAt }
  val scoreHistory = mutableMapOf<Player, MutableList<Int>>()
  val cumulativeScores = mutableMapOf<Player, Int>()
  val timelines = mutableMapOf<Player, MutableList<ScorePoint>>()
  val wins = mutableMapOf<Player, Int>()
  val placements = mutableMapOf<Player, Int>()

  sortedRounds.forEach { round ->
    val roundScores = Scores.roundScores(round, game)
    val ranking = roundScores.scores.entries.sortedByDescending { it.value }

    ranking.forEachIndexed { index, entry ->
      placements[entry.key] = (placements[entry.key] ?: 0) + (index + 1)
      if (index == 0) {
        wins[entry.key] = (wins[entry.key] ?: 0) + 1
      }
    }

    roundScores.scores.forEach { (player, score) ->
      scoreHistory.getOrPut(player) { mutableListOf() }.add(score)
      val updated = (cumulativeScores[player] ?: 0) + score
      cumulativeScores[player] = updated
      timelines.getOrPut(player) { mutableListOf() }.add(ScorePoint(round.createdAt, updated))
    }
  }

  return scoreHistory
    .map { (player, scores) ->
      val totalRounds = scores.size
      val totalScore = scores.sum()
      PlayerStats(
        player = player,
        totalRounds = totalRounds,
        totalScore = totalScore,
        averageScore = if (totalRounds > 0) scores.average().toFloat() else 0f,
        bestScore = scores.maxOrNull() ?: 0,
        worstScore = scores.minOrNull() ?: 0,
        wins = wins[player] ?: 0,
        winRate =
          if (totalRounds > 0) {
            ((wins[player] ?: 0) * 100f) / totalRounds
          } else {
            0f
          },
        averagePlacement =
          if (totalRounds > 0) {
            (placements[player]?.toFloat() ?: totalRounds.toFloat()) / totalRounds
          } else {
            0f
          },
        cumulativeTimeline = timelines[player].orEmpty(),
      )
    }
    .sortedBy { it.player.name.lowercase() }
}

private fun Float.formatScore(): String {
  val rounded = (this * 10f).roundToInt() / 10f
  return if (rounded % 1f == 0f) {
    rounded.toInt().toString()
  } else {
    rounded
      .toString()
      .trimEnd('0')
      .trimEnd('.')
  }
}

