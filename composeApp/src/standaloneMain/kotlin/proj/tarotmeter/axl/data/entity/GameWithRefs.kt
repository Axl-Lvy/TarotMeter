package proj.tarotmeter.axl.data.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import proj.tarotmeter.axl.data.model.Game

/**
 * Room relation data class representing a complete game with associated entities.
 *
 * @property game The main game entity.
 * @property rounds List of rounds belonging to this game.
 * @property players List of players participating in this game.
 */
data class GameWithRefs(
  @Embedded val game: GameEntity,
  @Relation(entity = RoundEntity::class, parentColumn = "game_id", entityColumn = "game_id")
  val rounds: List<RoundWithRefs>,
  @Relation(
    parentColumn = "game_id",
    entityColumn = "player_id",
    associateBy = Junction(GamePlayerCrossRef::class),
  )
  val players: List<PlayerEntity>,
) {
  /**
   * Converts this entity with references to a domain model Game.
   *
   * @return Game domain model with converted rounds and players.
   */
  fun toGame(): Game {
    return Game(
      game.id,
      players.map { it.toPlayer() },
      rounds.map { it.toRound() }.toMutableList(),
      game.startedAt,
      game.updatedAt,
    )
  }
}
