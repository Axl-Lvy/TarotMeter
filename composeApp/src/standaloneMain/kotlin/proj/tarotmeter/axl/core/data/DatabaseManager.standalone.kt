package proj.tarotmeter.axl.core.data

import kotlin.uuid.Uuid
import proj.tarotmeter.axl.core.data.entity.GameEntity
import proj.tarotmeter.axl.core.data.entity.GamePlayerCrossRef
import proj.tarotmeter.axl.core.data.entity.PlayerEntity
import proj.tarotmeter.axl.core.data.entity.RoundEntity
import proj.tarotmeter.axl.core.data.model.Game
import proj.tarotmeter.axl.core.data.model.Player
import proj.tarotmeter.axl.core.data.model.Round

/** DatabaseManager implementation for standalone platforms using Room database. */
internal class StandaloneLocalDatabaseManager(
  private val database: StandaloneLocalDatabase = getStandaloneLocalDatabase()
) : DatabaseManager {

  override suspend fun getPlayers(): List<Player> {
    return database.getPlayerDao().getAllPlayers().map { it.toPlayer() }
  }

  override suspend fun insertPlayer(player: Player) {
    database.getPlayerDao().insertPlayer(PlayerEntity(player.id, player.name, player.updatedAt))
  }

  override suspend fun renamePlayer(id: Uuid, newName: String) {
    database.getPlayerDao().renamePlayer(id, newName)
  }

  override suspend fun deletePlayer(id: Uuid) {
    database.getGameDao().deleteGamesFromPlayer(id)
    database.getPlayerDao().deletePlayer(id)
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
    database.getGameDao().insertGame(GameEntity(game.id, game.startedAt, game.updatedAt))
    for (player in game.players) {
      database.getGameDao().insertGamePlayerCrossRef(GamePlayerCrossRef(game.id, player.id))
    }
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
          round.chelem,
          round.updatedAt,
        )
      )
  }

  override suspend fun removeGame(id: Uuid) {
    database.getGameDao().deleteGame(id)
  }
}

actual fun getPlatformSpecificDatabaseManager(): DatabaseManager {
  return StandaloneLocalDatabaseManager()
}
