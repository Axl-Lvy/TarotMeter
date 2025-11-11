package proj.tarotmeter.axl.core.data.cloud

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlin.random.Random
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import proj.tarotmeter.axl.core.data.cloud.auth.AuthManager
import proj.tarotmeter.axl.core.data.cloud.model.SupabasePlayer
import proj.tarotmeter.axl.core.data.cloud.model.SupabaseRound
import proj.tarotmeter.axl.core.data.model.Game

/**
 * Manages games that are not owned by the current user but that the user can join via an invitation
 * code
 */
class ForeignerGamesManager : KoinComponent {
  private val authManager: AuthManager by inject()
  private val supabaseClient: SupabaseClient by inject()

  /**
   * Creates a game invitation for the given game id
   *
   * @param gameId The id of the game to create the invitation for
   * @return The invitation code created, or -1 if the user is not logged in
   */
  suspend fun createGameInvitation(gameId: Uuid): Int {
    if (authManager.user == null) {
      return -1
    }
    val invitationCode = Random.nextInt(1_0000_0000) // 8-digit code

    supabaseClient
      .from("game_invitation")
      .insert(SupabaseGameInvitation(gameId = gameId, invitationCode = invitationCode))
    return invitationCode
  }

  /**
   * Joins a game using the given invitation code
   *
   * @param invitationCode The invitation code to join the game
   */
  suspend fun joinGame(invitationCode: Int) {
    supabaseClient.postgrest.rpc(
      "join_game_with_invitation",
      SupabaseGameInvitationCode(invitationCode),
    )
  }

  /**
   * Gets all games that are not owned by the current user but that the user can access
   *
   * @return The list of non-owned games
   */
  suspend fun getNonOwnedGames(): List<Game> {
    val rpc = supabaseClient.postgrest.rpc("get_my_cross_games")
    print(rpc.data)
    val fetchResult = rpc.decodeList<SupabaseGameWithRefs>()
    return fetchResult.filter { !it.isDeleted }.map { it.toGame() }
  }
}

@Serializable
private data class SupabaseGameInvitation(
  @SerialName("game_id") val gameId: Uuid,
  @SerialName("invitation_code") val invitationCode: Int,
)

@Serializable
private data class SupabaseGameInvitationCode(
  @SerialName("p_invitation_code") val invitationCode: Int
)

@Serializable
private data class SupabaseGameWithRefs(
  @SerialName("game_id") val gameId: String,
  @SerialName("user_id") val userId: String,
  val name: String,
  @SerialName("updated_at") val updatedAt: Instant,
  @SerialName("created_at") val createdAt: Instant,
  @SerialName("is_deleted") val isDeleted: Boolean,
  val players: List<SupabasePlayer>,
  val rounds: List<SupabaseRound>,
) {
  fun toGame(): Game {
    val idToPlayer = players.associate { Pair(it.playerId, it.toPlayer()) }
    return Game(
      players = idToPlayer.values.sortedBy { it.updatedAt },
      name = name,
      id = Uuid.parse(gameId),
      roundsInternal =
        rounds
          .sortedBy { it.index }
          .map { it.toRound { id -> idToPlayer[id] ?: error("Player $id not found") } }
          .toMutableList(),
      startedAt = createdAt,
      updatedAtInternal = updatedAt,
    )
  }
}
