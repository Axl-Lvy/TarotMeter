package fr.axllvy.tarotmeter.core

import fr.axllvy.tarotmeter.core.data.DatabaseManager
import fr.axllvy.tarotmeter.core.data.model.Game
import fr.axllvy.tarotmeter.core.data.model.Player
import fr.axllvy.tarotmeter.util.TestWithKoin
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.test.runTest
import org.koin.core.component.inject

class TestRenameGame : TestWithKoin {
  private val dbManager: DatabaseManager by inject()

  @AfterTest fun cleanDb() = runTest { dbManager.clear() }

  private suspend fun DatabaseManager.clear() {
    val players = this.getPlayers()
    players.forEach { deletePlayer(it.id) }
  }

  @Test
  fun testRenameGame() = runTest {
    val players = listOf(Player("Alice"), Player("Bob"), Player("Charlie"))
    players.forEach { dbManager.insertPlayer(it) }

    val game = Game(players, name = "Original Name")
    dbManager.insertGame(game)

    // Verify original name
    val retrievedGame = dbManager.getGame(game.id)
    assertNotNull(retrievedGame)
    assertEquals("Original Name", retrievedGame.name)

    // Rename the game
    dbManager.renameGame(game.id, "New Name")

    // Verify new name
    val renamedGame = dbManager.getGame(game.id)
    assertNotNull(renamedGame)
    assertEquals("New Name", renamedGame.name)

    // Verify other properties unchanged
    assertEquals(game.id, renamedGame.id)
    assertEquals(3, renamedGame.players.size)
  }

  @Test
  fun testRenameGameMultipleTimes() = runTest {
    val players = listOf(Player("Player1"), Player("Player2"), Player("Player3"))
    players.forEach { dbManager.insertPlayer(it) }

    val game = Game(players, name = "First Name")
    dbManager.insertGame(game)

    // First rename
    dbManager.renameGame(game.id, "Second Name")
    var updatedGame = dbManager.getGame(game.id)
    assertNotNull(updatedGame)
    assertEquals("Second Name", updatedGame.name)

    // Second rename
    dbManager.renameGame(game.id, "Third Name")
    updatedGame = dbManager.getGame(game.id)
    assertNotNull(updatedGame)
    assertEquals("Third Name", updatedGame.name)

    // Third rename
    dbManager.renameGame(game.id, "Final Name")
    updatedGame = dbManager.getGame(game.id)
    assertNotNull(updatedGame)
    assertEquals("Final Name", updatedGame.name)
  }

  @Test
  fun testRenameGameWithSpecialCharacters() = runTest {
    val players = listOf(Player("Player1"), Player("Player2"), Player("Player3"))
    players.forEach { dbManager.insertPlayer(it) }

    val game = Game(players, name = "Simple Name")
    dbManager.insertGame(game)

    // Test with special characters
    val specialName = "Game with émojis 🎮 and symbols ✨"
    dbManager.renameGame(game.id, specialName)

    val updatedGame = dbManager.getGame(game.id)
    assertNotNull(updatedGame)
    assertEquals(specialName, updatedGame.name)
  }
}
