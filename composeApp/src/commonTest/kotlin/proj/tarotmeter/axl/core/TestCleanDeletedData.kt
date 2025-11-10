package proj.tarotmeter.axl.core

import io.kotest.assertions.nondeterministic.eventually
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.koin.core.component.inject
import proj.tarotmeter.axl.core.data.LocalDatabaseManager
import proj.tarotmeter.axl.core.data.cloud.CloudDatabaseManager
import proj.tarotmeter.axl.core.data.cloud.Uploader
import proj.tarotmeter.axl.core.data.model.Game
import proj.tarotmeter.axl.core.data.model.Player
import proj.tarotmeter.axl.core.data.model.Round
import proj.tarotmeter.axl.core.data.model.enums.Chelem
import proj.tarotmeter.axl.core.data.model.enums.Contract
import proj.tarotmeter.axl.core.data.model.enums.PetitAuBout
import proj.tarotmeter.axl.core.data.model.enums.Poignee
import proj.tarotmeter.axl.util.TEST_TIMEOUT
import proj.tarotmeter.axl.util.TestAuthenticated

/** Tests for cleaning deleted data after successful upload synchronization. */
class TestCleanDeletedData : TestAuthenticated() {
  private val uploader: Uploader by inject()
  private val localDb: LocalDatabaseManager by inject()
  private val cloudDb: CloudDatabaseManager by inject()

  @AfterTest
  @BeforeTest
  fun cleanDb() = runTestWithTrueClock {
    localDb.clearLocal()
    cloudDb.clearCloud()
  }

  private suspend fun LocalDatabaseManager.clearLocal() {
    clear()
    assertTrue(getPlayers().isEmpty())
    assertTrue(getGames().isEmpty())
  }

  private suspend fun CloudDatabaseManager.clearCloud() {
    hardDeleteGames()
    hardDeletePlayers()
    assertTrue(getPlayers().isEmpty())
    assertTrue(getGames().isEmpty())
  }

  private suspend fun awaitCloudPlayerAbsent(playerId: String) {
    eventually(TEST_TIMEOUT) {
      val cloudIds = cloudDb.getPlayers().map { it.id.toString() }.toSet()
      assertTrue { !cloudIds.contains(playerId) }
    }
  }

  private suspend fun awaitCloudGameAbsent(gameId: String) {
    eventually(TEST_TIMEOUT) {
      val cloudIds = cloudDb.getGames().map { it.id.toString() }.toSet()
      assertTrue { !cloudIds.contains(gameId) }
    }
  }

  @Test
  fun testDeletedPlayerIsCleanedAfterUpload() = runTestWithTrueClock {
    uploader.isActive = true

    val player = Player("ToDelete")
    localDb.insertPlayer(player)

    eventually(TEST_TIMEOUT) { assertTrue { cloudDb.getPlayers().any { it.id == player.id } } }

    localDb.deletePlayer(player.id)

    awaitCloudPlayerAbsent(player.id.toString())

    val playersIncludingDeleted = localDb.getPlayersUpdatedSince(Instant.DISTANT_PAST)
    assertTrue(playersIncludingDeleted.none { it.id == player.id && it.isDeleted })
  }

  @Test
  fun testDeletedGameIsCleanedAfterUpload() = runTestWithTrueClock {
    uploader.isActive = true

    val players = listOf(Player("A"), Player("B"), Player("C"))
    players.forEach { localDb.insertPlayer(it) }

    val game = Game(players, name = "Test Game")
    localDb.insertGame(game)

    eventually(TEST_TIMEOUT) { assertTrue { cloudDb.getGames().any { it.id == game.id } } }

    localDb.deleteGame(game.id)

    awaitCloudGameAbsent(game.id.toString())

    val gamesIncludingDeleted = localDb.getGamesUpdatedSince(Instant.DISTANT_PAST)
    assertTrue(gamesIncludingDeleted.none { it.id == game.id && it.isDeleted })
  }

  @Test
  fun testDeletedRoundIsCleanedAfterUpload() = runTestWithTrueClock {
    uploader.isActive = true

    val players = listOf(Player("A"), Player("B"), Player("C"), Player("D"))
    players.forEach { localDb.insertPlayer(it) }

    val game = Game(players, name = "Test Game")
    localDb.insertGame(game)

    val round =
      Round(
        taker = players[0],
        partner = null,
        contract = Contract.PETITE,
        oudlerCount = 1,
        takerPoints = 42,
        poignee = Poignee.NONE,
        petitAuBout = PetitAuBout.NONE,
        chelem = Chelem.NONE,
        index = 0,
      )
    localDb.addRound(game.id, round)

    eventually(TEST_TIMEOUT) {
      val cloudGame = cloudDb.getGame(game.id)
      assertTrue { cloudGame?.rounds?.any { it.id == round.id } == true }
    }

    localDb.deleteRound(round.id)

    eventually(TEST_TIMEOUT) {
      val cloudGame = cloudDb.getGame(game.id)
      assertTrue { cloudGame?.rounds?.none { it.id == round.id } == true }
    }

    val roundsIncludingDeleted = localDb.getRoundsUpdatedSince(Instant.DISTANT_PAST)
    assertTrue(roundsIncludingDeleted.none { it.id == round.id && it.isDeleted })
  }

  @Test
  fun testMultipleDeletedEntitiesCleanedAfterUpload() = runTestWithTrueClock {
    uploader.isActive = true

    val player1 = Player("Player1")
    val player2 = Player("Player2")
    val player3 = Player("Player3")
    val player4 = Player("Player4")

    localDb.insertPlayer(player1)
    localDb.insertPlayer(player2)
    localDb.insertPlayer(player3)
    localDb.insertPlayer(player4)

    val game1 = Game(listOf(player1, player2, player3), name = "Test Game 1")
    val game2 = Game(listOf(player2, player3, player4), name = "Test Game 2")

    localDb.insertGame(game1)
    localDb.insertGame(game2)

    eventually(TEST_TIMEOUT) {
      assertEquals(4, cloudDb.getPlayers().size)
      assertEquals(2, cloudDb.getGames().size)
    }

    localDb.deletePlayer(player1.id)
    localDb.deleteGame(game2.id)

    awaitCloudPlayerAbsent(player1.id.toString())
    awaitCloudGameAbsent(game2.id.toString())

    val playersIncludingDeleted = localDb.getPlayersUpdatedSince(Instant.DISTANT_PAST)
    val gamesIncludingDeleted = localDb.getGamesUpdatedSince(Instant.DISTANT_PAST)

    assertTrue(playersIncludingDeleted.none { it.id == player1.id && it.isDeleted })
    assertTrue(gamesIncludingDeleted.none { it.id == game2.id && it.isDeleted })

    assertEquals(3, localDb.getPlayers().size)
    assertEquals(0, localDb.getGames().size)
  }

  @Test
  fun testCleanDeletedDataDoesNotAffectActiveData() = runTestWithTrueClock {
    uploader.isActive = true

    val activePlayer = Player("Active")
    val deletedPlayer = Player("Deleted")

    localDb.insertPlayer(activePlayer)
    localDb.insertPlayer(deletedPlayer)

    eventually(TEST_TIMEOUT) { assertEquals(2, cloudDb.getPlayers().size) }

    localDb.deletePlayer(deletedPlayer.id)

    awaitCloudPlayerAbsent(deletedPlayer.id.toString())

    val remainingPlayers = localDb.getPlayers()
    assertEquals(1, remainingPlayers.size)
    assertEquals(activePlayer.id, remainingPlayers.first().id)
    assertEquals(activePlayer.name, remainingPlayers.first().name)
  }

  @Test
  fun testManualCleanDeletedDataCall() = runTestWithTrueClock {
    uploader.isActive = false

    val player = Player("ToDelete")
    localDb.insertPlayer(player)
    localDb.deletePlayer(player.id)

    var playersIncludingDeleted = localDb.getPlayersUpdatedSince(Instant.DISTANT_PAST)
    assertTrue(playersIncludingDeleted.any { it.id == player.id && it.isDeleted })

    localDb.cleanDeletedData(Instant.DISTANT_FUTURE)

    playersIncludingDeleted = localDb.getPlayersUpdatedSince(Instant.DISTANT_PAST)
    assertTrue(playersIncludingDeleted.none { it.id == player.id })
  }

  @Test
  fun testDeletedGameWithRoundsCleanedTogether() = runTestWithTrueClock {
    uploader.isActive = true

    val players = listOf(Player("A"), Player("B"), Player("C"), Player("D"))
    players.forEach { localDb.insertPlayer(it) }

    val game = Game(players, name = "Test Game")
    localDb.insertGame(game)

    val round1 =
      Round(
        taker = players[0],
        partner = null,
        contract = Contract.PETITE,
        oudlerCount = 1,
        takerPoints = 42,
        poignee = Poignee.NONE,
        petitAuBout = PetitAuBout.NONE,
        chelem = Chelem.NONE,
        index = 0,
      )
    val round2 =
      Round(
        taker = players[1],
        partner = null,
        contract = Contract.GARDE,
        oudlerCount = 2,
        takerPoints = 55,
        poignee = Poignee.SIMPLE,
        petitAuBout = PetitAuBout.TAKER,
        chelem = Chelem.NONE,
        index = 1,
      )

    localDb.addRound(game.id, round1)
    localDb.addRound(game.id, round2)

    eventually(TEST_TIMEOUT) {
      val cloudGame = cloudDb.getGame(game.id)
      assertEquals(2, cloudGame?.rounds?.size)
    }

    localDb.deleteGame(game.id)

    awaitCloudGameAbsent(game.id.toString())

    val gamesIncludingDeleted = localDb.getGamesUpdatedSince(Instant.DISTANT_PAST)
    val roundsIncludingDeleted = localDb.getRoundsUpdatedSince(Instant.DISTANT_PAST)

    assertTrue(gamesIncludingDeleted.none { it.id == game.id && it.isDeleted })
    assertTrue(roundsIncludingDeleted.none { it.gameId == game.id && it.isDeleted })
  }
}

private fun runTestWithTrueClock(block: suspend () -> Unit) = runTest {
  withContext(Dispatchers.Default) { block() }
}
