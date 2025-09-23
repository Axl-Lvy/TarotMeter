package proj.tarotmeter.axl.data

import kotlin.uuid.Uuid
import kotlinx.browser.window
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import proj.tarotmeter.axl.data.model.Game
import proj.tarotmeter.axl.data.model.Player
import proj.tarotmeter.axl.data.model.Round

/** LocalStorage-based implementation of DatabaseManager for web. */
object LocalStorageDatabaseManager : DatabaseManager {
  private const val PLAYERS_KEY = "tarotmeter_players"
  private const val GAMES_KEY = "tarotmeter_games"

  private val json = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
  }

  override suspend fun getPlayers(): List<Player> =
    withContext(Dispatchers.Default) {
      val raw = window.localStorage.getItem(PLAYERS_KEY) ?: return@withContext emptyList()
      runCatching { json.decodeFromString<List<Player>>(raw) }.getOrDefault(emptyList())
    }

  override suspend fun insertPlayer(player: Player) =
    withContext(Dispatchers.Default) {
      val players = getPlayers() + player
      window.localStorage.setItem(PLAYERS_KEY, json.encodeToString(players))
    }

  override suspend fun renamePlayer(id: Uuid, newName: String) =
    withContext(Dispatchers.Default) {
      val players = getPlayers().map { if (it.id == id) Player(newName, id) else it }
      window.localStorage.setItem(PLAYERS_KEY, json.encodeToString(players))
    }

  override suspend fun deletePlayer(id: Uuid) =
    withContext(Dispatchers.Default) {
      val players = getPlayers().filterNot { it.id == id }
      window.localStorage.setItem(PLAYERS_KEY, json.encodeToString(players))
    }

  override suspend fun getGames(): List<Game> =
    withContext(Dispatchers.Default) {
      val raw = window.localStorage.getItem(GAMES_KEY) ?: return@withContext emptyList()
      runCatching { json.decodeFromString<List<Game>>(raw) }.getOrDefault(emptyList())
    }

  override suspend fun getGame(id: Uuid): Game? =
    withContext(Dispatchers.Default) { getGames().find { it.id == id } }

  override suspend fun insertGame(game: Game) =
    withContext(Dispatchers.Default) {
      val games = getGames() + game
      window.localStorage.setItem(GAMES_KEY, json.encodeToString(games))
    }

  override suspend fun addRound(gameId: Uuid, round: Round) =
    withContext(Dispatchers.Default) {
      val games = getGames()
      games.forEach { if (it.id == gameId) it.addRound(round) }
      window.localStorage.setItem(GAMES_KEY, json.encodeToString(games))
    }

  override suspend fun removeGame(id: Uuid) =
    withContext(Dispatchers.Default) {
      val games = getGames().filterNot { it.id == id }
      window.localStorage.setItem(GAMES_KEY, json.encodeToString(games))
    }
}

actual fun getPlatformSpecificDatabaseManager(): DatabaseManager {
  return LocalStorageDatabaseManager
}
