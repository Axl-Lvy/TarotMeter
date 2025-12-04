package fr.tarotmeter.core.data

import fr.tarotmeter.core.data.entity.DeviceIdEntity
import fr.tarotmeter.core.data.entity.GameEntity
import fr.tarotmeter.core.data.entity.GamePlayerCrossRef
import fr.tarotmeter.core.data.entity.PlayerEntity
import fr.tarotmeter.core.data.entity.RoundEntity
import fr.tarotmeter.core.data.model.Game
import fr.tarotmeter.core.data.model.Player
import fr.tarotmeter.core.data.model.Round
import fr.tarotmeter.core.data.sync.GameSync
import fr.tarotmeter.core.data.sync.PlayerSync
import fr.tarotmeter.core.data.sync.RoundSync
import fr.tarotmeter.util.DateUtil
import kotlin.time.Instant
import kotlin.uuid.Uuid

/** DatabaseManager implementation for standalone platforms using Room database. */
internal class StandaloneDatabaseManager(
  private val database: StandaloneDatabase = getStandaloneLocalDatabase()
) : LocalDatabaseManager() {

  override suspend fun getPlayers(): List<Player> {
    return database.getPlayerDao().getAllPlayers().map { it.toPlayer() }
  }

  override suspend fun insertPlayer(player: Player) {
    database.getPlayerDao().insertPlayer(PlayerEntity(player.id, player.name, player.updatedAt))
    notifyChange()
  }

  override suspend fun renamePlayer(id: Uuid, newName: String) {
    database.getPlayerDao().renamePlayer(id, newName, DateUtil.now())
    notifyChange()
  }

  override suspend fun deletePlayer(id: Uuid) {
    val now = DateUtil.now()
    val deletedGames = database.getGameDao().getGameIdsFromPlayer(id)
    database.getGameDao().deleteGamesFromPlayer(id, now)
    deletedGames.forEach { database.getGameDao().deleteRoundsForGame(it) }
    database.getPlayerDao().deletePlayer(id, now)
    notifyChange()
  }

  override suspend fun getGames(): List<Game> {
    return database.getGameDao().getAllGames().map { it.toGame() }
  }

  override suspend fun getGame(id: Uuid): Game? {
    return database.getGameDao().getGame(id)?.toGame()
  }

  override suspend fun insertGame(game: Game) {
    for (player in game.players) {
      insertPlayer(player)
    }
    database.getGameDao().insertGame(GameEntity(game.id, game.name, game.startedAt, game.updatedAt))
    for (player in game.players) {
      database.getGameDao().insertGamePlayerCrossRef(GamePlayerCrossRef(game.id, player.id))
    }
    game.rounds.forEach { addRound(game.id, it) }
    notifyChange()
  }

  override suspend fun renameGame(id: Uuid, newName: String) {
    database.getGameDao().renameGame(id, newName, DateUtil.now())
    notifyChange()
  }

  override suspend fun addRound(gameId: Uuid, round: Round) {
    insertPlayer(round.taker)
    round.partner?.let { insertPlayer(it) }
    database
      .getGameDao()
      .insertRound(
        RoundEntity(
          round.id,
          gameId,
          round.taker.id,
          round.partner?.id,
          round.contract,
          round.oudlerCount,
          round.takerPoints,
          round.poignee,
          round.petitAuBout,
          round.index,
          round.chelem,
          round.updatedAt,
        )
      )
    // touch game timestamp
    database.getGameDao().touchGame(gameId, round.updatedAt)
    notifyChange()
  }

  override suspend fun deleteRound(roundId: Uuid) {
    database.getGameDao().deleteRound(roundId)
    notifyChange()
  }

  override suspend fun updateRound(round: Round) {
    val oldRound = database.getGameDao().getRound(round.id)
    checkNotNull(oldRound) { "Impossible to update a round that is not stored in the database." }
    addRound(oldRound.gameId, round)
  }

  override suspend fun deleteGame(id: Uuid) {
    database.getGameDao().deleteGame(id, DateUtil.now())
    notifyChange()
  }

  override suspend fun getPlayersUpdatedSince(since: Instant): List<PlayerSync> {
    return database.getPlayerDao().getPlayersUpdatedSince(since).map {
      PlayerSync(id = it.id, name = it.name, updatedAt = it.updatedAt, isDeleted = it.isDeleted)
    }
  }

  override suspend fun getGamesUpdatedSince(since: Instant): List<GameSync> {
    val dao = database.getGameDao()
    return dao.getGamesUpdatedSince(since).map { gameEntity ->
      GameSync(
        id = gameEntity.id,
        name = gameEntity.name,
        startedAt = gameEntity.startedAt,
        updatedAt = gameEntity.updatedAt,
        isDeleted = gameEntity.isDeleted,
        playerIds = dao.getPlayerIdsForGame(gameEntity.id),
      )
    }
  }

  override suspend fun getRoundsUpdatedSince(since: Instant): List<RoundSync> {
    return database.getGameDao().getRoundsUpdatedSince(since).map {
      RoundSync(
        id = it.id,
        gameId = it.gameId,
        takerId = it.takerId,
        partnerId = it.partnerId,
        contract = it.contract,
        oudlerCount = it.oudlerCount,
        takerPoints = it.takerPoints,
        poignee = it.poignee,
        petitAuBout = it.petitAuBout,
        index = it.index,
        chelem = it.chelem,
        updatedAt = it.updatedAt,
        isDeleted = it.isDeleted,
      )
    }
  }

  override suspend fun clear() {
    database.getPlayerDao().clearPlayers()
    database.getGameDao().clearGamePlayerCrossRef()
    database.getGameDao().clearGames()
  }

  override suspend fun cleanDeletedData(dateLimit: Instant) {
    database.getGameDao().cleanDeletedRounds(dateLimit)
    database.getGameDao().cleanDeletedGames(dateLimit)
    database.getPlayerDao().cleanDeletedPlayers(dateLimit)
  }

  override suspend fun getDeviceId(): Uuid? {
    return database.getMiscDao().getDeviceId()?.deviceId
  }

  override suspend fun insertDeviceId(deviceId: Uuid) {
    if (getDeviceId() != null) {
      error("Device ID is already set and cannot be changed.")
    }
    database.getMiscDao().insertDeviceId(DeviceIdEntity(deviceId))
  }
}

actual fun getPlatformSpecificDatabaseManager(): DatabaseManager {
  return StandaloneDatabaseManager()
}
