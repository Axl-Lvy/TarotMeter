package proj.tarotmeter.axl.core

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.koin.core.component.inject
import proj.tarotmeter.axl.core.data.LocalDatabaseManager
import proj.tarotmeter.axl.core.data.cloud.CloudDatabaseManager
import proj.tarotmeter.axl.core.data.cloud.Downloader
import proj.tarotmeter.axl.core.data.cloud.Uploader
import proj.tarotmeter.axl.core.data.model.Game
import proj.tarotmeter.axl.core.data.model.Player
import proj.tarotmeter.axl.util.TestAuthenticated

/** Tests covering the downloader logic (full refresh + merge) including deletions. */
class TestDownloaderSynchronization : TestAuthenticated() {
  private val downloader: Downloader by inject()
  private val cloudDb: CloudDatabaseManager by inject()
  private val localDb: LocalDatabaseManager by inject()
  private val uploader: Uploader by inject()

  @AfterTest
  @BeforeTest
  fun cleanup() = runTest {
    uploader.forceDeactivate = true
    localDb.clear()
    cloudDb.hardDeleteGames()
    cloudDb.hardDeletePlayers()
  }

  @Test
  fun testFullRefreshDownloadsRemoteState() = runTest {
    uploader.forceDeactivate = true
    // Prepare remote state
    val players = listOf(Player("R1"), Player("R2"), Player("R3"))
    players.forEach { cloudDb.insertPlayer(it) }
    val game = Game(players, name = "Test Game")
    cloudDb.insertGame(game)

    // Local initially empty
    assertTrue(localDb.getPlayers().isEmpty())
    assertTrue(localDb.getGames().isEmpty())

    downloader.downloadData(clearLocal = true)

    assertEquals(players.map { it.id }.toSet(), localDb.getPlayers().map { it.id }.toSet())
    val localGames = localDb.getGames()
    assertEquals(1, localGames.size)
    assertEquals(game.id, localGames.first().id)
  }

  @Test
  fun testMergeModeAppliesRemoteDeletions() = runTest {
    uploader.forceDeactivate = true
    // Remote initial
    val a = Player("A")
    val b = Player("B")
    cloudDb.insertPlayer(a)
    cloudDb.insertPlayer(b)

    // Initial full download
    downloader.downloadData(clearLocal = true)
    assertEquals(setOf(a.id, b.id), localDb.getPlayers().map { it.id }.toSet())

    // Remote deletion of player B
    cloudDb.deletePlayer(b.id)

    // Merge download (no clear)
    downloader.downloadData(clearLocal = false)

    val remainingIds = localDb.getPlayers().map { it.id }.toSet()
    assertEquals(
      setOf(a.id),
      remainingIds,
      "Deleted remote player should be removed locally in merge mode",
    )
  }

  @Test
  fun testMergeModeAddsNewRemoteEntitiesAndDeletesOld() = runTest {
    uploader.forceDeactivate = true
    val p1 = Player("P1")
    val p2 = Player("P2")
    cloudDb.insertPlayer(p1)
    cloudDb.insertPlayer(p2)
    downloader.downloadData(clearLocal = true)
    assertEquals(setOf(p1.id, p2.id), localDb.getPlayers().map { it.id }.toSet())

    // Delete p2 remotely and add p3
    cloudDb.deletePlayer(p2.id)
    val p3 = Player("P3")
    cloudDb.insertPlayer(p3)

    downloader.downloadData(clearLocal = false)

    val ids = localDb.getPlayers().map { it.id }.toSet()
    assertEquals(
      setOf(p1.id, p3.id),
      ids,
      "Merge should add new remote player and remove deleted one",
    )
  }

  @Test
  fun testMergeRemovesDeletedGame() = runTest {
    uploader.forceDeactivate = true
    val players = listOf(Player("G1"), Player("G2"), Player("G3"))
    players.forEach { cloudDb.insertPlayer(it) }
    val game = Game(players, name = "Test Game")
    cloudDb.insertGame(game)
    downloader.downloadData(clearLocal = true)
    assertEquals(1, localDb.getGames().size)

    // Soft delete game remotely
    cloudDb.deleteGame(game.id)

    // Merge download should remove it locally
    downloader.downloadData(clearLocal = false)
    assertTrue(localDb.getGames().isEmpty(), "Deleted remote game should be removed locally")
  }
}
