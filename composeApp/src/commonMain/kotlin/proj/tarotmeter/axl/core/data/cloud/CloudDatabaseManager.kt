package proj.tarotmeter.axl.core.data.cloud

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.PostgrestFilterDSL
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.PostgrestRequestBuilder
import io.github.jan.supabase.postgrest.query.filter.PostgrestFilterBuilder
import io.github.jan.supabase.postgrest.result.PostgrestResult
import kotlin.uuid.Uuid
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import proj.tarotmeter.axl.core.data.DatabaseManager
import proj.tarotmeter.axl.core.data.cloud.auth.AuthManager
import proj.tarotmeter.axl.core.data.cloud.model.SupabaseGame
import proj.tarotmeter.axl.core.data.cloud.model.SupabaseGameCrossPlayer
import proj.tarotmeter.axl.core.data.cloud.model.SupabasePlayer
import proj.tarotmeter.axl.core.data.cloud.model.SupabaseRound
import proj.tarotmeter.axl.core.data.model.Game
import proj.tarotmeter.axl.core.data.model.Player
import proj.tarotmeter.axl.core.data.model.Round

/**
 * CloudDatabaseManager implementation using Supabase as the backend.
 *
 * This class provides methods to interact with the Supabase database for managing players and
 * games. It ensures that all operations are performed in the context of the authenticated user.
 */
class CloudDatabaseManager : DatabaseManager, KoinComponent {

  private val supabaseClient: SupabaseClient by inject()
  private val authManager: AuthManager by inject()

  private suspend fun selectForUser(
    table: String,
    block: @PostgrestFilterDSL (PostgrestFilterBuilder.() -> Unit) = {},
  ): PostgrestResult? {
    val user = authManager.user ?: return null
    return supabaseClient.from(table).select {
      filter {
        and {
          eq("user_id", user.id)
          block()
        }
      }
    }
  }

  private fun PostgrestRequestBuilder.filterForUser(block: PostgrestFilterBuilder.() -> Unit) {
    val user = authManager.user ?: return
    filter {
      and {
        eq("user_id", user.id)
        block()
      }
    }
  }

  override suspend fun getPlayers(): List<Player> {
    if (authManager.user == null) return emptyList()
    return selectForUser("player")?.decodeList<SupabasePlayer>()?.map { it.toPlayer() }
      ?: emptyList()
  }

  suspend fun getPlayer(id: Uuid): Player? {
    val userId = authManager.user?.id ?: return null
    val select =
      supabaseClient.from("player").select {
        filter {
          and {
            eq("user_id", userId)
            eq("player_id", id.toString())
          }
        }
      }
    return select.decodeSingleOrNull<SupabasePlayer>()?.toPlayer()
  }

  override suspend fun insertPlayer(player: Player) {
    val user = authManager.user ?: return
    supabaseClient
      .from("player")
      .insert(
        SupabasePlayer(
          playerId = player.id.toString(),
          name = player.name,
          updatedAt = player.updatedAt,
          userId = user.id,
        )
      )
  }

  override suspend fun renamePlayer(id: Uuid, newName: String) {
    if (authManager.user == null) return
    supabaseClient.from("player").update(mapOf("name" to newName)) {
      filterForUser { eq("player_id", id.toString()) }
    }
  }

  override suspend fun deletePlayer(id: Uuid) {
    if (authManager.user == null) return
    // Step 1: Find all game_ids where this player is referenced
    val gameCrossPlayers =
      supabaseClient
        .from("game_cross_player")
        .select { filter { eq("player_id", id.toString()) } }
        .decodeList<SupabaseGameCrossPlayer>()

    // Step 2: Delete games where game_id is in gameIds
    if (gameCrossPlayers.isNotEmpty()) {
      supabaseClient.from("game").delete {
        filterForUser { isIn("game_id", gameCrossPlayers.map { it.gameId }) }
      }
    }

    // Step 3: Delete the player itself
    supabaseClient.from("player").delete { filterForUser { eq("player_id", id.toString()) } }
  }

  override suspend fun getGames(): List<Game> {
    if (authManager.user == null) return emptyList()
    val supabaseGames =
      selectForUser("game")?.decodeList<SupabaseGame>()?.associateBy { it.gameId }
        ?: return emptyList()
    val supabaseCrossRefs =
      supabaseClient
        .from("game_cross_player")
        .select { filter { isIn("game_id", supabaseGames.keys.toList()) } }
        .decodeList<SupabaseGameCrossPlayer>()
        .groupBy { it.gameId }
    val supabasePlayers =
      supabaseClient
        .from("player")
        .select {
          filterForUser {
            isIn("player_id", supabaseCrossRefs.values.flatten().map { it.playerId }.distinct())
          }
        }
        .decodeList<SupabasePlayer>()
        .associateBy { it.playerId }
    val supabaseRounds =
      supabaseClient
        .from("round")
        .select { filter { isIn("game_id", supabaseGames.keys.toList()) } }
        .decodeList<SupabaseRound>()
        .groupBy { it.gameId }
    return supabaseGames.map {
      val players =
        supabaseCrossRefs[it.key]?.mapNotNull { crossRef ->
          supabasePlayers[crossRef.playerId]?.toPlayer()
        } ?: emptyList()
      val rounds =
        supabaseRounds[it.key]?.map { round ->
          round.toRound { playerId ->
            supabasePlayers[playerId]?.toPlayer() ?: error { "No player found with id $playerId" }
          }
        } ?: emptyList()
      Game(
        players,
        Uuid.parse(it.value.gameId),
        rounds.toMutableList(),
        it.value.createdAt,
        it.value.updatedAt,
      )
    }
  }

  override suspend fun getGame(id: Uuid): Game? {
    if (authManager.user == null) return null
    val supabaseGame =
      supabaseClient
        .from("game")
        .select { filter { eq("game_id", id.toString()) } }
        .decodeSingleOrNull<SupabaseGame>() ?: return null
    val supabaseCrossRefs =
      supabaseClient
        .from("game_cross_player")
        .select { filter { eq("game_id", supabaseGame.gameId) } }
        .decodeList<SupabaseGameCrossPlayer>()
        .groupBy { it.gameId }
    val supabasePlayers =
      supabaseClient
        .from("player")
        .select {
          filterForUser {
            isIn("player_id", supabaseCrossRefs.values.flatten().map { it.playerId }.distinct())
          }
        }
        .decodeList<SupabasePlayer>()
    val supabaseRounds =
      supabaseClient
        .from("round")
        .select { filter { eq("game_id", supabaseGame.gameId) } }
        .decodeList<SupabaseRound>()
    val players = supabasePlayers.map { it.toPlayer() }.associateBy { it.id.toString() }
    val rounds =
      supabaseRounds.map {
        it.toRound { playerId ->
          players[playerId] ?: error { "No player found with id $playerId" }
        }
      }
    return Game(
      players.values.toList(),
      Uuid.parse(supabaseGame.gameId),
      rounds.toMutableList(),
      supabaseGame.createdAt,
      supabaseGame.updatedAt,
    )
  }

  override suspend fun insertGame(game: Game) {
    val user = authManager.user ?: return
    // Insert all players in bulk
    if (game.players.isNotEmpty()) {
      val playerDtos =
        game.players.map { player ->
          SupabasePlayer(
            playerId = player.id.toString(),
            name = player.name,
            updatedAt = player.updatedAt,
            userId = user.id,
          )
        }
      supabaseClient.from("player").upsert(playerDtos)
    }
    // Insert the game
    supabaseClient
      .from("game")
      .insert(
        SupabaseGame(
          gameId = game.id.toString(),
          userId = user.id,
          updatedAt = game.updatedAt,
          createdAt = game.startedAt,
        )
      )
    // Insert cross-references for each player in bulk
    if (game.players.isNotEmpty()) {
      val crossRefs =
        game.players.map { player ->
          SupabaseGameCrossPlayer(
            playerId = player.id.toString(),
            updatedAt = game.updatedAt,
            gameId = game.id.toString(),
          )
        }
      supabaseClient.from("game_cross_player").insert(crossRefs)
    }
    // Insert rounds in bulk
    if (game.rounds.isNotEmpty()) {
      val roundDtos =
        game.rounds.map { round ->
          SupabaseRound(
            roundId = round.id.toString(),
            updatedAt = round.updatedAt,
            taker = round.taker.id.toString(),
            partner = round.partner?.id?.toString(),
            contract = round.contract,
            oudlerCount = round.oudlerCount,
            takerPoints = round.takerPoints,
            poignee = round.poignee,
            petitAuBout = round.petitAuBout,
            chelem = round.chelem,
            gameId = game.id.toString(),
          )
        }
      supabaseClient.from("round").insert(roundDtos)
    }
  }

  override suspend fun addRound(gameId: Uuid, round: Round) {
    if (authManager.user == null) return
    supabaseClient
      .from("round")
      .insert(
        SupabaseRound(
          roundId = round.id.toString(),
          updatedAt = round.updatedAt,
          taker = round.taker.id.toString(),
          partner = round.partner?.id?.toString(),
          contract = round.contract,
          oudlerCount = round.oudlerCount,
          takerPoints = round.takerPoints,
          poignee = round.poignee,
          petitAuBout = round.petitAuBout,
          chelem = round.chelem,
          gameId = gameId.toString(),
        )
      )
  }

  override suspend fun removeGame(id: Uuid) {
    if (authManager.user == null) return
    supabaseClient.from("game").delete { filterForUser { eq("game_id", id.toString()) } }
  }
}
