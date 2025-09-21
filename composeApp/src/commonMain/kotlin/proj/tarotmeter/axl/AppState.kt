package proj.tarotmeter.axl

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import proj.tarotmeter.axl.model.*
import kotlin.random.Random

sealed class Screen {
    data object Home: Screen()
    data object Players: Screen()
    data object Settings: Screen()
    data object NewGame: Screen()
    data object History: Screen()
    data class GameEditor(val gameId: String): Screen()
}

class AppState {
    // Simple in-memory stores
    val players = mutableStateListOf<Player>()
    val games = mutableStateListOf<Game>()

    var currentScreen: Screen by mutableStateOf(Screen.Home)
        private set

    init {
        // Seed with some sample players for quicker testing
        if (players.isEmpty()) {
            repeat(5) { i ->
                players.add(Player(id = "P${i+1}", name = "Player ${i+1}"))
            }
        }
    }

    fun navigate(screen: Screen) { currentScreen = screen }

    fun addPlayer(name: String) {
        val id = generateId("P")
        players.add(Player(id, name))
    }

    fun renamePlayer(id: String, newName: String) {
        val idx = players.indexOfFirst { it.id == id }
        if (idx >= 0) players[idx] = players[idx].copy(name = newName)
    }

    fun removePlayer(id: String) {
        players.removeAll { it.id == id }
    }

    fun createGame(playerCount: Int): Game? {
        if (players.size < playerCount) return null
        val selected = players.take(playerCount)
        val game = Game(id = generateId("G"), players = selected.toList())
        games.add(0, game)
        currentScreen = Screen.GameEditor(game.id)
        return game
    }

    fun getGame(id: String): Game? = games.find { it.id == id }

    fun addRound(gameId: String, round: Round) {
        val game = getGame(gameId) ?: return
        val computed = round.copy(scores = computeRoundScores(game, round))
        game.rounds.add(computed)
    }

    fun totalScore(game: Game, playerId: String): Int =
        game.rounds.sumOf { it.scores[playerId] ?: 0 }

    private fun generateId(prefix: String): String =
        buildString {
            append(prefix)
            append("-")
            append(Random.nextInt(1000, 9999))
            append("-")
            append(idCounter++)
        }

    companion object {
        private var idCounter: Int = 1
    }
}
