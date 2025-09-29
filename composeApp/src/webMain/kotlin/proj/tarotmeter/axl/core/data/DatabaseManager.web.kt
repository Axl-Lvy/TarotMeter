package proj.tarotmeter.axl.core.data

import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import proj.tarotmeter.axl.core.data.model.Game
import proj.tarotmeter.axl.core.data.model.Player
import proj.tarotmeter.axl.core.data.model.Round
import proj.tarotmeter.axl.core.data.sync.GameSync
import proj.tarotmeter.axl.core.data.sync.PlayerSync
import proj.tarotmeter.axl.core.data.sync.RoundSync
import proj.tarotmeter.axl.util.DateUtil

private const val PLAYERS_KEY = "tarotmeter_players"
private const val GAMES_KEY = "tarotmeter_games"

/** LocalStorage-based implementation of DatabaseManager for web. */
class LocalStorageDatabaseManager(
  private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default
) : LocalDatabaseManager() {

  private val json = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
  }

  override suspend fun getPlayers(): List<Player> =
    getPlayerEntities().filter { !it.isDeleted }.map { it.toPlayer() }

  override suspend fun insertPlayer(player: Player) {
    withContext(coroutineDispatcher) {
      val players =
        getPlayerEntities() + PlayerLocalStorage(player.name, player.id, player.updatedAt)
      window.localStorage.setItem(PLAYERS_KEY, json.encodeToString(players))
    }
    notifyChange()
  }

  override suspend fun renamePlayer(id: Uuid, newName: String) {
    withContext(coroutineDispatcher) {
      val players =
        getPlayerEntities().map {
          if (it.id == id) PlayerLocalStorage(newName, id, DateUtil.now()) else it
        }
      window.localStorage.setItem(PLAYERS_KEY, json.encodeToString(players))
    }
    notifyChange()
  }

  override suspend fun deletePlayer(id: Uuid) {
    withContext(coroutineDispatcher) {
      val players =
        getPlayerEntities().map {
          if (it.id == id) it.copy(updatedAt = DateUtil.now(), isDeleted = true) else it
        }
      window.localStorage.setItem(PLAYERS_KEY, json.encodeToString(players))
    }
    notifyChange()
  }

  private suspend fun getPlayerEntities(): List<PlayerLocalStorage> =
    withContext(coroutineDispatcher) {
      val raw = window.localStorage.getItem(PLAYERS_KEY) ?: return@withContext emptyList()
      runCatching { json.decodeFromString<List<PlayerLocalStorage>>(raw) }.getOrDefault(emptyList())
    }

  override suspend fun getGame(id: Uuid): Game? =
    withContext(coroutineDispatcher) { getGames().find { it.id == id } }

  override suspend fun getGames(): List<Game> =
    getGameEntities().filter { !it.isDeleted }.map { it.toGame() }

  override suspend fun insertGame(game: Game) {
    withContext(coroutineDispatcher) {
      val games =
        getGameEntities() +
          GameLocalStorage(
            game.players.map { PlayerLocalStorage(it.name, it.id, it.updatedAt) },
            game.id,
            game.rounds
              .map {
                RoundLocalStorage(
                  PlayerLocalStorage(it.taker.name, it.taker.id, it.taker.updatedAt),
                  PlayerLocalStorage(it.taker.name, it.taker.id, it.taker.updatedAt),
                  it.contract,
                  it.oudlerCount,
                  it.takerPoints,
                  it.poignee,
                  it.petitAuBout,
                  it.chelem,
                  it.id,
                )
              }
              .toMutableList(),
            game.startedAt,
            game.updatedAt,
          )
      window.localStorage.setItem(GAMES_KEY, json.encodeToString(games))
    }
    notifyChange()
  }

  override suspend fun addRound(gameId: Uuid, round: Round) {
    withContext(coroutineDispatcher) {
      val games = getGameEntities()
      games.forEach {
        if (it.id == gameId) {
          it.roundsInternal.add(
            RoundLocalStorage(
              PlayerLocalStorage(round.taker.name, round.taker.id, round.taker.updatedAt),
              PlayerLocalStorage(round.taker.name, round.taker.id, round.taker.updatedAt),
              round.contract,
              round.oudlerCount,
              round.takerPoints,
              round.poignee,
              round.petitAuBout,
              round.chelem,
              round.id,
            )
          )
          it.updatedAtInternal = DateUtil.now()
        }
      }
      window.localStorage.setItem(GAMES_KEY, json.encodeToString(games))
    }
    notifyChange()
  }

  override suspend fun deleteGame(id: Uuid) {
    withContext(coroutineDispatcher) {
      val games =
        getGameEntities().map {
          if (it.id == id)
            it.copy(
              isDeleted = true,
              roundsInternal =
                it.roundsInternal.map { round -> round.copy(isDeleted = true) }.toMutableList(),
            )
          else it
        }
      window.localStorage.setItem(GAMES_KEY, json.encodeToString(games))
    }
    notifyChange()
  }

  private suspend fun getGameEntities(): List<GameLocalStorage> =
    withContext(coroutineDispatcher) {
      val raw = window.localStorage.getItem(GAMES_KEY) ?: return@withContext emptyList()
      runCatching { json.decodeFromString<List<GameLocalStorage>>(raw) }.getOrDefault(emptyList())
    }

  // --- Sync overrides (best-effort; deletions not tracked in web local storage) ---
  override suspend fun getPlayersUpdatedSince(since: Instant): List<PlayerSync> =
    withContext(coroutineDispatcher) {
      getPlayerEntities()
        .filter { it.updatedAt > since }
        .map {
          PlayerSync(id = it.id, name = it.name, updatedAt = it.updatedAt, isDeleted = it.isDeleted)
        }
    }

  override suspend fun getGamesUpdatedSince(since: Instant): List<GameSync> =
    withContext(coroutineDispatcher) {
      getGameEntities()
        .filter { it.updatedAtInternal > since }
        .map { game ->
          GameSync(
            id = game.id,
            startedAt = game.startedAt,
            updatedAt = game.updatedAtInternal,
            isDeleted = game.isDeleted,
            playerIds = game.players.map { it.id },
          )
        }
    }

  override suspend fun getRoundsUpdatedSince(since: Instant): List<RoundSync> =
    withContext(coroutineDispatcher) {
      getGameEntities().flatMap { game ->
        game.roundsInternal
          .filter { it.updatedAt > since }
          .map { round ->
            RoundSync(
              id = round.id,
              gameId = game.id,
              takerId = round.taker.id,
              partnerId = round.partner?.id,
              contract = round.contract,
              oudlerCount = round.oudlerCount,
              takerPoints = round.takerPoints,
              poignee = round.poignee,
              petitAuBout = round.petitAuBout,
              chelem = round.chelem,
              updatedAt = round.updatedAt,
              isDeleted = round.isDeleted,
            )
          }
      }
    }

  override suspend fun clear() {
    withContext(coroutineDispatcher) {
      window.localStorage.removeItem(PLAYERS_KEY)
      window.localStorage.removeItem(GAMES_KEY)
    }
    notifyChange()
  }
}

actual fun getPlatformSpecificDatabaseManager(): DatabaseManager {
  return LocalStorageDatabaseManager()
}
