package fr.axllvy.tarotmeter.core

import fr.axllvy.tarotmeter.core.data.LocalDatabaseManager
import fr.axllvy.tarotmeter.core.data.cloud.CloudDatabaseManager
import fr.axllvy.tarotmeter.core.data.cloud.Downloader
import fr.axllvy.tarotmeter.core.data.cloud.Uploader
import fr.axllvy.tarotmeter.core.data.model.Game
import fr.axllvy.tarotmeter.core.data.model.Player
import fr.axllvy.tarotmeter.util.TestAuthenticated
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.koin.core.component.inject

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
    uploader.forceDeactivate = false
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

    downloader.downloadData(fullSync = true)

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
    downloader.downloadData(fullSync = true)
    assertEquals(setOf(a.id, b.id), localDb.getPlayers().map { it.id }.toSet())

    // Remote deletion of player B
    cloudDb.deletePlayer(b.id)

    // Merge download (no clear)
    downloader.downloadData(fullSync = false)

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
    downloader.downloadData(fullSync = true)
    assertEquals(setOf(p1.id, p2.id), localDb.getPlayers().map { it.id }.toSet())

    // Delete p2 remotely and add p3
    cloudDb.deletePlayer(p2.id)
    val p3 = Player("P3")
    cloudDb.insertPlayer(p3)

    downloader.downloadData(fullSync = false)

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
    downloader.downloadData(fullSync = true)
    assertEquals(1, localDb.getGames().size)

    // Soft delete game remotely
    cloudDb.deleteGame(game.id)

    // Merge download should remove it locally
    downloader.downloadData(fullSync = false)
    assertTrue(localDb.getGames().isEmpty(), "Deleted remote game should be removed locally")
  }

  @Test
  fun testIncrementalSyncOnlyDownloadsNewChanges() = runTest {
    uploader.forceDeactivate = true
    // Initial state
    val p1 = Player("P1")
    val p2 = Player("P2")
    cloudDb.insertPlayer(p1)
    cloudDb.insertPlayer(p2)

    // First full sync
    downloader.downloadData(fullSync = true)
    assertEquals(2, localDb.getPlayers().size)

    // Add new player remotely
    val p3 = Player("P3")
    cloudDb.insertPlayer(p3)

    // Incremental sync should only fetch p3
    downloader.downloadData(fullSync = false)
    assertEquals(3, localDb.getPlayers().size)
    assertTrue(localDb.getPlayers().any { it.id == p3.id })
  }

  @Test
  fun testIncrementalSyncWithNoChangesDoesNothing() = runTest {
    uploader.forceDeactivate = true
    val player = Player("TestPlayer")
    cloudDb.insertPlayer(player)

    downloader.downloadData(fullSync = true)
    assertEquals(1, localDb.getPlayers().size)

    // Second incremental sync with no changes
    downloader.downloadData(fullSync = false)
    assertEquals(1, localDb.getPlayers().size)
  }

  @Test
  fun testRemoteLastSyncIsUpdatedAfterFullSync() = runTest {
    uploader.forceDeactivate = true
    val deviceId = localDb.getOrCreateDeviceId()
    val player = Player("TestPlayer")
    cloudDb.insertPlayer(player)

    downloader.downloadData(fullSync = true)

    val lastRemoteSync = cloudDb.getLastRemoteSync(deviceId)
    assertTrue(
      lastRemoteSync > player.updatedAt,
      "Remote last_sync should be updated after full sync",
    )
  }

  @Test
  fun testRemoteLastSyncIsUpdatedAfterIncrementalSync() = runTest {
    uploader.forceDeactivate = true
    val deviceId = localDb.getOrCreateDeviceId()
    val p1 = Player("P1")
    cloudDb.insertPlayer(p1)
    downloader.downloadData(fullSync = true)

    val initialLastSync = cloudDb.getLastRemoteSync(deviceId)

    // Add new player
    val p2 = Player("P2")
    cloudDb.insertPlayer(p2)
    downloader.downloadData(fullSync = false)

    val updatedLastSync = cloudDb.getLastRemoteSync(deviceId)
    assertTrue(
      updatedLastSync > initialLastSync,
      "Remote last_sync should advance after incremental sync",
    )
    assertTrue(updatedLastSync > p2.updatedAt, "Remote last_sync should be ahead of latest entity")
  }

  @Test
  fun testIncrementalSyncHandlesPlayerDeletion() = runTest {
    uploader.forceDeactivate = true
    val p1 = Player("P1")
    val p2 = Player("P2")
    cloudDb.insertPlayer(p1)
    cloudDb.insertPlayer(p2)

    downloader.downloadData(fullSync = true)
    assertEquals(2, localDb.getPlayers().size)

    // Delete player remotely
    cloudDb.deletePlayer(p2.id)

    // Incremental sync should apply deletion
    downloader.downloadData(fullSync = false)
    assertEquals(1, localDb.getPlayers().size)
    assertEquals(p1.id, localDb.getPlayers().first().id)
  }

  @Test
  fun testIncrementalSyncHandlesGameDeletion() = runTest {
    uploader.forceDeactivate = true
    val players = listOf(Player("P1"), Player("P2"), Player("P3"))
    players.forEach { cloudDb.insertPlayer(it) }
    val game = Game(players, name = "TestGame")
    cloudDb.insertGame(game)

    downloader.downloadData(fullSync = true)
    assertEquals(1, localDb.getGames().size)

    // Delete game remotely
    cloudDb.deleteGame(game.id)

    // Incremental sync should apply deletion
    downloader.downloadData(fullSync = false)
    assertTrue(localDb.getGames().isEmpty())
  }

  @Test
  fun testIncrementalSyncHandlesMultipleChanges() = runTest {
    uploader.forceDeactivate = true
    val p1 = Player("P1")
    val p2 = Player("P2")
    cloudDb.insertPlayer(p1)
    cloudDb.insertPlayer(p2)

    downloader.downloadData(fullSync = true)
    assertEquals(2, localDb.getPlayers().size)

    // Multiple changes: delete p2, add p3 and p4
    cloudDb.deletePlayer(p2.id)
    val p3 = Player("P3")
    val p4 = Player("P4")
    cloudDb.insertPlayer(p3)
    cloudDb.insertPlayer(p4)

    // Incremental sync should handle all changes
    downloader.downloadData(fullSync = false)
    val playerIds = localDb.getPlayers().map { it.id }.toSet()
    assertEquals(setOf(p1.id, p3.id, p4.id), playerIds, "Should have p1, p3, p4 but not deleted p2")
  }

  @Test
  fun testFullSyncClearsLocalDatabase() = runTest {
    uploader.forceDeactivate = true
    // Add local data
    val localPlayer = Player("LocalOnly")
    localDb.insertPlayer(localPlayer)
    assertEquals(1, localDb.getPlayers().size)

    // Remote has different data
    val remotePlayer = Player("RemoteOnly")
    cloudDb.insertPlayer(remotePlayer)

    // Full sync should clear local and replace with remote
    downloader.downloadData(fullSync = true)
    val playerIds = localDb.getPlayers().map { it.id }
    assertEquals(1, playerIds.size)
    assertEquals(remotePlayer.id, playerIds.first())
  }

  @Test
  fun testIncrementalSyncDoesNotDownloadAlreadySyncedData() = runTest {
    uploader.forceDeactivate = true
    val p1 = Player("P1")
    cloudDb.insertPlayer(p1)

    // First sync
    downloader.downloadData(fullSync = true)
    val deviceId = localDb.getOrCreateDeviceId()
    val firstSyncTimestamp = cloudDb.getLastRemoteSync(deviceId)

    // Second incremental sync without changes
    downloader.downloadData(fullSync = false)
    val secondSyncTimestamp = cloudDb.getLastRemoteSync(deviceId)

    // Timestamp should not change if there are no new changes
    assertEquals(firstSyncTimestamp, secondSyncTimestamp)
  }

  @Test
  fun testIncrementalSyncHandlesGameUpdate() = runTest {
    uploader.forceDeactivate = true
    val players = listOf(Player("P1"), Player("P2"), Player("P3"))
    players.forEach { cloudDb.insertPlayer(it) }
    val game = Game(players, name = "OriginalName")
    cloudDb.insertGame(game)

    downloader.downloadData(fullSync = true)
    assertEquals("OriginalName", localDb.getGames().first().name)

    // Update game remotely
    val updatedGame = game.copy(name = "UpdatedName")
    cloudDb.renameGame(updatedGame.id, updatedGame.name)

    // Incremental sync should update the game
    downloader.downloadData(fullSync = false)
    assertEquals("UpdatedName", localDb.getGames().first().name)
  }
}
