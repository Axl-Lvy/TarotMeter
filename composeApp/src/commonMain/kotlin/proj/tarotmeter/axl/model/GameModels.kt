package proj.tarotmeter.axl.model

import androidx.compose.runtime.Immutable

@Immutable
data class Player(
    val id: String,
    val name: String,
)

@Immutable
data class Game(
    val id: String,
    val players: List<Player>,
    val rounds: MutableList<Round> = mutableListOf(),
    val startedAtMillis: Long = getNowMillis(),
    var isActive: Boolean = true,
)

@Immutable
data class Round(
    val takerId: String,
    val partnerId: String? = null,
    val contract: Contract,
    val oudlerCount: Int,
    val cardPoints: Int, // 0..91
    val notes: String = "",
    val scores: Map<String, Int> = emptyMap(), // computed per playerId
)

enum class Contract(val multiplier: Int, val title: String) {
    Petite(1, "Petite"),
    Garde(2, "Garde"),
    GardeSans(4, "Garde Sans"),
    GardeContre(6, "Garde Contre");
}

fun getTargetForOudlers(oudlers: Int): Int = when (oudlers) {
    0 -> 56
    1 -> 51
    2 -> 41
    else -> 36
}

fun computeRoundScores(game: Game, round: Round): Map<String, Int> {
    val n = game.players.size
    require(n in 3..5) { "Only 3 to 5 players supported" }
    val target = getTargetForOudlers(round.oudlerCount)
    val diff = round.cardPoints - target
    val sign = if (diff >= 0) 1 else -1
    val base = 25 + kotlin.math.abs(diff)
    val value = base * round.contract.multiplier * sign

    val taker = round.takerId
    val partner = round.partnerId

    val result = mutableMapOf<String, Int>()
    game.players.forEach { result[it.id] = 0 }

    if (n == 3) {
        // taker vs 2 defenders
        result[taker] = value * 2
        game.players.filter { it.id != taker }.forEach { result[it.id] = -value }
    } else if (n == 4) {
        // taker vs 3 defenders
        result[taker] = value * 3
        game.players.filter { it.id != taker }.forEach { result[it.id] = -value }
    } else { // n == 5
        // If partner is provided, distribute 2/1 among taker/partner vs 3 defenders
        val defenders = game.players.map { it.id }.toMutableList().apply {
            remove(taker)
            partner?.let { remove(it) }
        }
        defenders.forEach { result[it] = -value }
        if (partner == null) {
            // No partner case: treat like 4 defenders (not standard but fallback)
            result[taker] = value * 4
        } else {
            result[taker] = value * 2
            result[partner] = (result[partner] ?: 0) + value
        }
    }
    return result
}

fun getNowMillis(): Long = 0L // Placeholder without external time APIs
