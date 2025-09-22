package proj.tarotmeter.axl.data

import proj.tarotmeter.axl.data.entity.GameEntity
import proj.tarotmeter.axl.data.entity.GamePlayerCrossRef
import proj.tarotmeter.axl.data.entity.PlayerEntity
import proj.tarotmeter.axl.data.entity.RoundEntity
import proj.tarotmeter.axl.data.model.Game
import proj.tarotmeter.axl.data.model.Player
import proj.tarotmeter.axl.data.model.Round

/** DatabaseManager implementation for standalone platforms using Room database. */
internal class StandaloneLocalDatabaseManager(
  private val database: StandaloneLocalDatabase = getStandaloneLocalDatabase()
) : DatabaseManager {

  override suspend fun getPlayers(): List<Player> {
    return database.getPlayerDao().getAllPlayers().map { it.toPlayer() }
  }

  override suspend fun insertPlayer(player: Player) {
    database.getPlayerDao().insertPlayer(PlayerEntity(player.id, player.name))
  }

  override suspend fun renamePlayer(id: Int, newName: String) {
    database.getPlayerDao().renamePlayer(id, newName)
  }

  override suspend fun deletePlayer(id: Int) {
    database.getGameDao().deleteGamesFromPlayer(id)
    database.getPlayerDao().deletePlayer(id)
  }

  override suspend fun getGames(): List<Game> {
    return database.getGameDao().getAllGames().map { it.toGame() }
  }

  override suspend fun getGame(id: Int): Game? {
    return database.getGameDao().getGame(id)?.toGame()
  }

  override suspend fun insertGame(game: Game) {
    for (player in game.players) {
      insertPlayer(player)
    }
    database.getGameDao().insertGame(GameEntity(game.id, game.startedAt, game.updatedAt))
    for (player in game.players) {
      database.getGameDao().insertGamePlayerCrossRef(GamePlayerCrossRef(game.id, player.id))
    }
  }

  override suspend fun addRound(gameId: Int, round: Round) {
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
          round.chelem,
        )
      )
  }

  override suspend fun removeGame(id: Int) {
    database.getGameDao().deleteGame(id)
  }

  override suspend fun getMaxPlayerId(): Int {
    return database.getPlayerDao().getMaxPlayerId() ?: 0
  }

  override suspend fun getMaxRoundId(): Int {
    return database.getGameDao().getMaxRoundId() ?: 0
  }

  override suspend fun getMaxGameId(): Int {
    return database.getGameDao().getMaxGameId() ?: 0
  }
}

actual fun getPlatformSpecificDatabaseManager(): DatabaseManager {
  return StandaloneLocalDatabaseManager()
}
