package fr.axllvy.tarotmeter.core.data.model.calculated

import androidx.compose.runtime.Immutable

/**
 * Represents a point in the cumulative score timeline.
 *
 * @property roundIndex The round index.
 * @property value The cumulative score at this round.
 */
@Immutable data class ScorePoint(val roundIndex: Int, val value: Int)
