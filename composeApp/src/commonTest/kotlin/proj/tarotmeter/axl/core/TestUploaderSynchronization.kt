package proj.tarotmeter.axl.core

import io.kotest.assertions.nondeterministic.eventually
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration
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

class TestUploaderSynchronization : TestAuthenticated() {
  private val uploader: Uploader by inject()
  private val localDb: LocalDatabaseManager by inject()
  private val cloudDb: CloudDatabaseManager by inject()

  @AfterTest
  @BeforeTest
  fun cleanDb() = runTestWithTrueClock {
    localDb.clearLocal()
    cloudDb.clearCloud()
  }

  // --- Helpers -----------------------------------------------------------------------------
  private suspend fun LocalDatabaseManager.clearLocal() {
    clear()
    assertTrue(getPlayers().isEmpty())
    assertTrue(getGames().isEmpty())
  }

  private suspend fun CloudDatabaseManager.clearCloud() {
    // Hard delete everything for a clean slate
    hardDeleteGames()
    hardDeletePlayers()
    assertTrue(getPlayers().isEmpty())
    assertTrue(getGames().isEmpty())
  }

  private suspend fun awaitCloudPlayersMatch(timeoutMs: Duration = TEST_TIMEOUT) {
    eventually(timeoutMs) {
      val localIds = localDb.getPlayers().map { it.id }.toSet()
      val cloudIds = cloudDb.getPlayers().map { it.id }.toSet()
      assertTrue { cloudIds == localIds }
    }
  }

  private suspend fun awaitCloudGamesMatch(timeoutMs: Duration = TEST_TIMEOUT) {
    eventually(timeoutMs) {
      val localGames = localDb.getGames()
      val cloudGames = cloudDb.getGames()
      val sameSize = localGames.size == cloudGames.size
      val sameIds = localGames.map { it.id }.toSet() == cloudGames.map { it.id }.toSet()
      val perGameRoundsMatch =
        localGames.all { lg ->
          val cg = cloudGames.find { it.id == lg.id }
          cg != null &&
            cg.rounds.size == lg.rounds.size &&
            cg.players.map { it.id }.toSet() == lg.players.map { it.id }.toSet()
        }
      assertTrue { sameSize && sameIds && perGameRoundsMatch }
    }
  }

  private suspend fun awaitCloudPlayerAbsent(playerId: String, timeoutMs: Duration = TEST_TIMEOUT) {
    eventually(timeoutMs) {
      val cloudIds = cloudDb.getPlayers().map { it.id.toString() }.toSet()
      assertFalse { cloudIds.contains(playerId) }
    }
  }

  // --- Tests -------------------------------------------------------------------------------

  @Test
  fun testBurstPlayerInsertionSynchronizesAll() = runTestWithTrueClock {
    uploader.isActive = true

    val players = (0 until 6).map { Player("P$it") }
    players.forEach { localDb.insertPlayer(it) }

    awaitCloudPlayersMatch()

    val cloudPlayers = cloudDb.getPlayers()
    assertEquals(players.size, cloudPlayers.size)
    assertEquals(players.map { it.id }.toSet(), cloudPlayers.map { it.id }.toSet())
  }

  @Test
  fun testSequentialPlayerInsertionsSynchronize() = runTestWithTrueClock {
    uploader.isActive = true

    val a = Player("A")
    localDb.insertPlayer(a)
    awaitCloudPlayersMatch()

    val b = Player("B")
    localDb.insertPlayer(b)
    awaitCloudPlayersMatch()

    val cloudPlayers = cloudDb.getPlayers().map { it.id }.toSet()
    assertEquals(setOf(a.id, b.id), cloudPlayers)
  }

  @Test
  fun testPlayerDeletionPropagates() = runTestWithTrueClock {
    uploader.isActive = true

    val p = Player("ToDelete")
    localDb.insertPlayer(p)
    awaitCloudPlayersMatch()

    localDb.deletePlayer(p.id)
    awaitCloudPlayerAbsent(p.id.toString())

    // Ensure local also no longer returns it
    assertTrue(localDb.getPlayers().none { it.id == p.id })
    assertTrue(cloudDb.getPlayers().none { it.id == p.id })
  }

  @Test
  fun testGameAndRoundsSynchronization() = runTestWithTrueClock {
    uploader.isActive = true

    val players = listOf(Player("A"), Player("B"), Player("C"), Player("D"))
    players.forEach { localDb.insertPlayer(it) }
    awaitCloudPlayersMatch()

    val game = Game(players, name = "Test Game")
    localDb.insertGame(game)

    // Add rounds after insertion
    val r1 =
      Round(
        taker = players[0],
        partner = null,
        contract = Contract.PETITE,
        oudlerCount = 1,
        takerPoints = 42,
        poignee = Poignee.NONE,
        petitAuBout = PetitAuBout.NONE,
        chelem = Chelem.NONE,
        index = 4,
      )
    val r2 =
      Round(
        taker = players[1],
        partner = null,
        contract = Contract.GARDE,
        oudlerCount = 2,
        takerPoints = 55,
        poignee = Poignee.SIMPLE,
        petitAuBout = PetitAuBout.TAKER,
        chelem = Chelem.NONE,
        index = 5,
      )
    localDb.addRound(game.id, r1)
    localDb.addRound(game.id, r2)

    awaitCloudGamesMatch()

    val cloudGame = cloudDb.getGame(game.id)
    assertEquals(game.id, cloudGame?.id, "Game IDs should match")
    assertEquals(
      players.map { it.id }.toSet(),
      cloudGame?.players?.map { it.id }?.toSet(),
      "Game player IDs should match",
    )
    assertEquals(2, cloudGame?.rounds?.size, "There should be 2 rounds in the cloud game")
  }
}

private fun runTestWithTrueClock(block: suspend () -> Unit) = runTest {
  withContext(Dispatchers.Default) { block() }
}
