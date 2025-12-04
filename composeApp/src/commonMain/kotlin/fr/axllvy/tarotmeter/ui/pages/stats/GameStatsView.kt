package fr.axllvy.tarotmeter.ui.pages.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import fr.axllvy.tarotmeter.core.data.model.Game
import fr.axllvy.tarotmeter.core.data.model.Player
import fr.axllvy.tarotmeter.core.data.model.calculated.PlayerStats
import fr.axllvy.tarotmeter.ui.components.PlayerAvatar
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource
import tarotmeter.composeapp.generated.resources.Res
import tarotmeter.composeapp.generated.resources.game_stats_chart_empty
import tarotmeter.composeapp.generated.resources.game_stats_chart_title
import tarotmeter.composeapp.generated.resources.game_stats_empty_state_message
import tarotmeter.composeapp.generated.resources.game_stats_empty_state_title
import tarotmeter.composeapp.generated.resources.game_stats_metric_avg_score
import tarotmeter.composeapp.generated.resources.game_stats_metric_best_score
import tarotmeter.composeapp.generated.resources.game_stats_metric_total_rounds
import tarotmeter.composeapp.generated.resources.game_stats_metric_win_rate
import tarotmeter.composeapp.generated.resources.game_stats_metric_worst_score

/**
 * Displays game statistics for players in a specific game.
 *
 * Shows score evolution over rounds, total score, and various metrics for each player.
 *
 * @param game The game to display statistics for
 * @param modifier Modifier for the component
 */
@Composable
fun GameStatsView(game: Game, modifier: Modifier = Modifier) {
  val playerStats = remember(game) { PlayerStats.from(game) }

  if (playerStats.isEmpty()) {
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
    remember(playerStats, palette) {
      playerStats
        .mapIndexed { index, stats -> stats.player to palette[index % palette.size] }
        .toMap()
    }

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(bottom = 32.dp),
  ) {
    item {
      PlayerScoreChart(
        stats = playerStats,
        playerColors = playerColors,
        modifier = Modifier.fillMaxWidth(),
      )
    }
    items(playerStats, key = { it.player.id }) { stats ->
      PlayerStatsCard(
        playerStats = stats,
        accentColor = playerColors[stats.player] ?: MaterialTheme.colorScheme.primary,
      )
    }
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
    Surface(modifier = modifier, shape = MaterialTheme.shapes.medium, tonalElevation = 2.dp) {
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
  val minX = allPoints.minOf { it.roundIndex }.toFloat()
  val maxX = allPoints.maxOf { it.roundIndex }.toFloat()
  val minY = min(0f, allPoints.minOf { it.value }.toFloat())
  val maxY = max(0f, allPoints.maxOf { it.value }.toFloat())
  val xRange = (maxX - minX).takeUnless { it == 0f } ?: 1f
  val yRange = (maxY - minY).takeUnless { it == 0f } ?: 1f

  val axisColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
  val defaultLineColor = MaterialTheme.colorScheme.primary

  // Prepare ticks (limit to ~12 labels)
  val roundIndices = (minX.toInt()..maxX.toInt()).toList()
  val tickStep = (roundIndices.size / 12).takeIf { it > 0 } ?: 1
  val tickIndices = roundIndices.filter { (it - minX.toInt()) % tickStep == 0 }

  Surface(modifier = modifier, shape = MaterialTheme.shapes.medium, tonalElevation = 2.dp) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text(
        text = stringResource(Res.string.game_stats_chart_title),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
      )
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Canvas(modifier = Modifier.fillMaxWidth().height(220.dp)) {
          val padding = 32.dp.toPx()
          val chartWidth = size.width - (padding * 2)
          val chartHeight = size.height - (padding * 2)
          val origin = Offset(padding, size.height - padding)
          // Y axis
          drawLine(
            color = axisColor,
            start = origin,
            end = Offset(padding, padding),
            strokeWidth = 2f,
          )
          // X axis
          drawLine(
            color = axisColor,
            start = origin,
            end = Offset(size.width - padding, size.height - padding),
            strokeWidth = 2f,
          )

          // Horizontal reference lines (quartiles)
          repeat(4) { i ->
            val frac = i / 4f
            val yValue = padding + (chartHeight * (1f - frac))
            drawLine(
              color = axisColor.copy(alpha = 0.15f),
              start = Offset(padding, yValue),
              end = Offset(size.width - padding, yValue),
              strokeWidth = 1f,
            )
          }

          // Draw player lines and points
          timelines.forEach { playerStat ->
            val color = playerColors[playerStat.player] ?: defaultLineColor
            val path = Path()
            playerStat.cumulativeTimeline.forEachIndexed { index, point ->
              val xRatio = (point.roundIndex - minX) / xRange
              val yRatio = (point.value - minY) / yRange
              val x = padding + (xRatio * chartWidth)
              val y = (size.height - padding) - (yRatio * chartHeight)
              if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path = path, color = color, style = Stroke(width = 3f))
            playerStat.cumulativeTimeline.forEach { point ->
              val xRatio = (point.roundIndex - minX) / xRange
              val yRatio = (point.value - minY) / yRange
              val x = padding + (xRatio * chartWidth)
              val y = (size.height - padding) - (yRatio * chartHeight)
              drawCircle(color = color, radius = 4.dp.toPx(), center = Offset(x, y))
            }
          }
        }
        // X axis labels
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          tickIndices.forEach { idx ->
            Text(
              text = idx.toString(),
              style = MaterialTheme.typography.labelSmall,
              color = axisColor,
            )
          }
        }
        // Y axis min/max labels
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text(
            text = minY.roundToInt().toString(),
            style = MaterialTheme.typography.labelSmall,
            color = axisColor,
          )
          Text(
            text = maxY.roundToInt().toString(),
            style = MaterialTheme.typography.labelSmall,
            color = axisColor,
          )
        }
      }

      timelines.forEach { playerStat ->
        val color = playerColors[playerStat.player] ?: defaultLineColor
        LegendItem(label = playerStat.player.name, color = color)
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
    Box(modifier = Modifier.size(12.dp), contentAlignment = Alignment.Center) {
      Surface(color = color, shape = MaterialTheme.shapes.small) { Spacer(Modifier.size(12.dp)) }
    }
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
            text =
              stringResource(Res.string.game_stats_metric_total_rounds) +
                ": ${playerStats.totalRounds}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      // Role metrics row (taker / partner / defender)
      RoleMetricsRow(playerStats)

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
      }
    }
  }
}

@Composable
private fun RoleMetricsRow(playerStats: PlayerStats) {
  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Text(
      text = "Roles",
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      RoleMetricChip(
        label = "Taker",
        count = playerStats.takerCount,
        winRate = playerStats.takerWinRate,
        modifier = Modifier.weight(1f),
      )
      if (playerStats.partnerCount > 0) {
        RoleMetricChip(
          label = "Partner",
          count = playerStats.partnerCount,
          winRate = playerStats.partnerWinRate,
          modifier = Modifier.weight(1f),
        )
      }
      RoleMetricChip(
        label = "Defender",
        count = playerStats.defenderCount,
        winRate = playerStats.defenderWinRate,
        modifier = Modifier.weight(1f),
      )
    }
  }
}

@Composable
private fun RoleMetricChip(
  label: String,
  count: Int,
  winRate: Float,
  modifier: Modifier = Modifier,
) {
  val roleColor =
    when (label) {
      "Taker" -> MaterialTheme.colorScheme.primary
      "Partner" -> MaterialTheme.colorScheme.secondary
      else -> MaterialTheme.colorScheme.tertiary
    }
  Surface(
    modifier = modifier,
    color = MaterialTheme.colorScheme.surfaceVariant,
    shape = MaterialTheme.shapes.small,
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      val barFraction = if (count == 0) 0f else (winRate / 100f).coerceIn(0f, 1f)
      // Simplified progress bar without clip / fillMaxHeight
      Box(
        modifier =
          Modifier.fillMaxWidth().height(6.dp).background(MaterialTheme.colorScheme.surface)
      ) {
        if (barFraction > 0f) {
          Box(
            modifier =
              Modifier.height(6.dp)
                .fillMaxWidth(barFraction)
                .background(roleColor.copy(alpha = 0.85f))
          )
        }
      }
      val rateText = if (count == 0) "—" else "${winRate.roundToInt()}%"
      Text(
        text = "${count}× ($rateText)",
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
      )
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

private fun Float.formatScore(): String {
  val rounded = (this * 10f).roundToInt() / 10f
  return if (rounded % 1f == 0f) {
    rounded.toInt().toString()
  } else {
    rounded.toString().trimEnd('0').trimEnd('.')
  }
}
