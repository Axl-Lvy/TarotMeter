package proj.tarotmeter.axl

import androidx.compose.runtime.mutableStateListOf
import proj.tarotmeter.axl.model.*

class AppState {
  // Simple in-memory stores
  val players = mutableStateListOf<Player>()
  val games = mutableStateListOf<Game>()

  init {
    // Seed with some sample players for quicker testing
    if (players.isEmpty()) {
      repeat(5) { i -> players.add(Player(name = "Player ${i+1}")) }
    }
  }

  fun addPlayer(name: String) {
    players.add(Player(name))
  }

  fun renamePlayer(id: Int, newName: String) {
    val idx = players.indexOfFirst { it.id == id }
    if (idx >= 0) players[idx].rename(newName)
  }

  fun removePlayer(id: Int) {
    players.removeAll { it.id == id }
  }

  fun createGame(playerCount: Int): Game? {
    if (players.size < playerCount) return null
    val selected = players.take(playerCount)
    val game = Game(players = selected.toList())
    games.add(0, game)
    return game
  }

  fun getGame(id: Int): Game? = games.find { it.id == id }

  fun addRound(gameId: Int, round: Round) {
    val game = getGame(gameId) ?: return
    game.rounds.add(round)
  }

  fun totalScore(game: Game, player: Player): Int = Scores.globalScores(game).scores[player] ?: 0
}
