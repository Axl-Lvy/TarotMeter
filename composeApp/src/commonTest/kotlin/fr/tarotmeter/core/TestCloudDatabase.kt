package fr.tarotmeter.core

import fr.tarotmeter.core.data.cloud.CloudDatabaseManager
import fr.tarotmeter.core.data.model.Game
import fr.tarotmeter.core.data.model.Player
import fr.tarotmeter.core.data.model.Round
import fr.tarotmeter.core.data.model.enums.Chelem
import fr.tarotmeter.core.data.model.enums.Contract
import fr.tarotmeter.core.data.model.enums.PetitAuBout
import fr.tarotmeter.core.data.model.enums.Poignee
import fr.tarotmeter.util.TestAuthenticated
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.koin.core.component.inject

class TestCloudDatabase : TestAuthenticated() {
  private val databaseManager: CloudDatabaseManager by inject()

  @AfterTest
  fun cleanDb() = runTest {
    databaseManager.clear()
    assertTrue(databaseManager.getPlayers().isEmpty())
    assertTrue(databaseManager.getGames().isEmpty())
  }

  private suspend fun CloudDatabaseManager.clear() {
    hardDeletePlayers()
    hardDeleteGames()
  }

  @Test
  fun testInsertAndGetPlayers() = runTest {
    val player1 = Player("Alice")
    val player2 = Player("Bob")

    databaseManager.insertPlayer(player1)
    databaseManager.insertPlayer(player2)

    val players = databaseManager.getPlayers()
    assertNotNull(databaseManager.getPlayer(player1.id))

    assertTrue(players.size >= 2)
    assertTrue(players.any { it.name == "Alice" })
    assertTrue(players.any { it.name == "Bob" })
  }

  @Test
  fun testRenamePlayer() = runTest {
    val player = Player("Charlie")
    databaseManager.insertPlayer(player)

    databaseManager.renamePlayer(player.id, "Charles")

    val players = databaseManager.getPlayers()
    val renamedPlayer = players.find { it.id == player.id }

    assertNotNull(renamedPlayer)
    assertEquals("Charles", renamedPlayer.name)
  }

  @Test
  fun `test deleting a player deletes its games`() = runTest {
    val players = listOf(Player("Player1"), Player("Player2"), Player("Player3"))

    players.forEach { databaseManager.insertPlayer(it) }

    val game = Game(players, name = "Test Game")
    databaseManager.insertGame(game)

    databaseManager.deletePlayer(players.first().id)
    assertTrue(databaseManager.getGames().isEmpty())
    assertEquals(databaseManager.getPlayers().size, 2)
  }

  @Test
  fun testInsertAndGetGames() = runTest {
    val players = listOf(Player("Player1"), Player("Player2"), Player("Player3"))

    players.forEach { databaseManager.insertPlayer(it) }

    val game = Game(players, name = "Test Game")
    databaseManager.insertGame(game)

    val games = databaseManager.getGames()
    assertTrue(games.isNotEmpty())

    val insertedGame = games.find { it.id == game.id }
    assertNotNull(insertedGame)
    assertEquals(3, insertedGame.players.size)
  }

  @Test
  fun testGetGameById() = runTest {
    val players = listOf(Player("Player1"), Player("Player2"), Player("Player3"), Player("Player4"))

    players.forEach { databaseManager.insertPlayer(it) }

    val game = Game(players, name = "Test Game")
    databaseManager.insertGame(game)

    val retrievedGame = databaseManager.getGame(game.id)
    assertNotNull(retrievedGame)
    assertEquals(game.id, retrievedGame.id)
    assertEquals(4, retrievedGame.players.size)
  }

  @Test
  fun `test adding a round with partner`() = runTest {
    val players =
      listOf(
        Player("Player1"),
        Player("Player2"),
        Player("Player3"),
        Player("Player4"),
        Player("Player5"),
      )

    players.forEach { databaseManager.insertPlayer(it) }

    val game = Game(players, name = "Test Game")
    databaseManager.insertGame(game)

    val round =
      Round(
        taker = players[0],
        contract = Contract.GARDE,
        partner = players[1],
        oudlerCount = 2,
        takerPoints = 48,
        poignee = Poignee.SIMPLE,
        petitAuBout = PetitAuBout.TAKER,
        chelem = Chelem.NONE,
        index = 0,
      )

    databaseManager.addRound(game.id, round)

    val updatedGame = databaseManager.getGame(game.id)
    assertNotNull(updatedGame)
    assertEquals(1, updatedGame.rounds.size)

    val addedRound = updatedGame.rounds.first()
    assertEquals(players[0].name, addedRound.taker.name)
    assertEquals(Contract.GARDE, addedRound.contract)
    assertEquals(2, addedRound.oudlerCount)
    assertEquals(48, addedRound.takerPoints)
  }

  @Test
  fun `test adding multiple rounds without partner to a game`() = runTest {
    val players = listOf(Player("Player1"), Player("Player2"), Player("Player3"))

    players.forEach { databaseManager.insertPlayer(it) }

    val game = Game(players, name = "Test Game")
    databaseManager.insertGame(game)

    val round1 =
      Round(
        taker = players[0],
        contract = Contract.PETITE,
        partner = null,
        oudlerCount = 1,
        takerPoints = 40,
        poignee = Poignee.NONE,
        petitAuBout = PetitAuBout.NONE,
        chelem = Chelem.NONE,
        index = 0,
      )

    val round2 =
      Round(
        taker = players[1],
        contract = Contract.GARDE_SANS,
        partner = null,
        oudlerCount = 3,
        takerPoints = 55,
        poignee = Poignee.DOUBLE,
        petitAuBout = PetitAuBout.DEFENSE,
        chelem = Chelem.ANNOUNCED,
        index = 1,
      )

    databaseManager.addRound(game.id, round1)
    databaseManager.addRound(game.id, round2)

    val updatedGame = databaseManager.getGame(game.id)
    assertNotNull(updatedGame)
    assertEquals(2, updatedGame.rounds.size)
  }

  @Test
  fun testRemoveGame() = runTest {
    val players = listOf(Player("Player1"), Player("Player2"), Player("Player3"))

    players.forEach { databaseManager.insertPlayer(it) }

    val game = Game(players, name = "Test Game")
    databaseManager.insertGame(game)

    val gamesBeforeRemoval = databaseManager.getGames()
    assertTrue(gamesBeforeRemoval.any { it.id == game.id })

    databaseManager.deleteGame(game.id)

    val gamesAfterRemoval = databaseManager.getGames()
    assertTrue(gamesAfterRemoval.none { it.id == game.id })

    val removedGame = databaseManager.getGame(game.id)
    assertNull(removedGame)
  }

  @Test
  fun testRoundWithAllEnumValues() = runTest {
    val players =
      listOf(
        Player("Player1"),
        Player("Player2"),
        Player("Player3"),
        Player("Player4"),
        Player("Player5"),
      )

    players.forEach { databaseManager.insertPlayer(it) }

    val game = Game(players, name = "Test Game")
    databaseManager.insertGame(game)

    val round =
      Round(
        taker = players[2],
        contract = Contract.GARDE_CONTRE,
        partner = players[1],
        oudlerCount = 3,
        takerPoints = 70,
        poignee = Poignee.TRIPLE,
        petitAuBout = PetitAuBout.DEFENSE,
        chelem = Chelem.FAILED,
        index = 0,
      )

    databaseManager.addRound(game.id, round)

    val updatedGame = databaseManager.getGame(game.id)
    assertNotNull(updatedGame)

    val addedRound = updatedGame.rounds.first()
    assertEquals(Contract.GARDE_CONTRE, addedRound.contract)
    assertEquals(Poignee.TRIPLE, addedRound.poignee)
    assertEquals(PetitAuBout.DEFENSE, addedRound.petitAuBout)
    assertEquals(Chelem.FAILED, addedRound.chelem)
    assertEquals(3, addedRound.oudlerCount)
    assertEquals(70, addedRound.takerPoints)
    assertNotNull(addedRound.partner)
    assertEquals(players[1].name, addedRound.partner.name)
  }

  @Test
  fun testGameWithMinimumPlayers() = runTest {
    val players = listOf(Player("Player1"), Player("Player2"), Player("Player3"))

    players.forEach { databaseManager.insertPlayer(it) }

    val game = Game(players, name = "Test Game")
    databaseManager.insertGame(game)

    val retrievedGame = databaseManager.getGame(game.id)
    assertNotNull(retrievedGame)
    assertEquals(3, retrievedGame.players.size)
  }

  @Test
  fun testGameWithMaximumPlayers() = runTest {
    val players =
      listOf(
        Player("Player1"),
        Player("Player2"),
        Player("Player3"),
        Player("Player4"),
        Player("Player5"),
      )

    players.forEach { databaseManager.insertPlayer(it) }

    val game = Game(players, name = "Test Game")
    databaseManager.insertGame(game)

    val retrievedGame = databaseManager.getGame(game.id)
    assertNotNull(retrievedGame)
    assertEquals(5, retrievedGame.players.size)
  }

  @Test
  fun testInsertGameWithRounds() = runTest {
    val players = listOf(Player("Player1"), Player("Player2"), Player("Player3"), Player("Player4"))

    players.forEach { databaseManager.insertPlayer(it) }

    val round1 =
      Round(
        taker = players[0],
        contract = Contract.PETITE,
        partner = null,
        oudlerCount = 1,
        takerPoints = 40,
        poignee = Poignee.NONE,
        petitAuBout = PetitAuBout.NONE,
        chelem = Chelem.NONE,
        index = 0,
      )

    val round2 =
      Round(
        taker = players[1],
        contract = Contract.GARDE,
        partner = null,
        oudlerCount = 2,
        takerPoints = 55,
        poignee = Poignee.SIMPLE,
        petitAuBout = PetitAuBout.TAKER,
        chelem = Chelem.NONE,
        index = 1,
      )

    val game =
      Game(players, "Test Game").apply {
        addRound(round1)
        addRound(round2)
      }

    databaseManager.insertGame(game)

    val retrievedGame = databaseManager.getGame(game.id)
    assertNotNull(retrievedGame)
    assertEquals(4, retrievedGame.players.size)
    assertEquals(2, retrievedGame.rounds.size)

    val retrievedRound1 = retrievedGame.rounds.find { it.id == round1.id }
    val retrievedRound2 = retrievedGame.rounds.find { it.id == round2.id }

    assertNotNull(retrievedRound1)
    assertNotNull(retrievedRound2)
    assertEquals(Contract.PETITE, retrievedRound1.contract)
    assertEquals(Contract.GARDE, retrievedRound2.contract)
  }

  @Test
  fun testBulkOperations() = runTest {
    val players = (1..5).map { Player("Player$it") }

    // Test bulk player insertion
    players.forEach { databaseManager.insertPlayer(it) }

    val game = Game(players, name = "Test Game")
    databaseManager.insertGame(game)

    // Verify all players and game were inserted
    val retrievedPlayers = databaseManager.getPlayers()
    assertTrue(retrievedPlayers.size >= 5)
    players.forEach { player -> assertTrue(retrievedPlayers.any { it.name == player.name }) }

    val retrievedGame = databaseManager.getGame(game.id)
    assertNotNull(retrievedGame)
    assertEquals(5, retrievedGame.players.size)
  }

  @Test
  fun testDeleteRound() = runTest {
    val players = listOf(Player("Player1"), Player("Player2"), Player("Player3"))
    players.forEach { databaseManager.insertPlayer(it) }

    val game = Game(players, name = "Test Game")
    databaseManager.insertGame(game)

    val round =
      Round(
        taker = players[0],
        contract = Contract.GARDE,
        partner = null,
        oudlerCount = 2,
        takerPoints = 48,
        poignee = Poignee.NONE,
        petitAuBout = PetitAuBout.NONE,
        chelem = Chelem.NONE,
        index = 0,
      )

    databaseManager.addRound(game.id, round)
    databaseManager.deleteRound(round.id)

    val updatedGame = databaseManager.getGame(game.id)
    assertNotNull(updatedGame)
    assertTrue(updatedGame.rounds.isEmpty())
  }

  @Test
  fun testUpdateRound() = runTest {
    val players = listOf(Player("Player1"), Player("Player2"), Player("Player3"))
    players.forEach { databaseManager.insertPlayer(it) }

    val game = Game(players, name = "Test Game")
    databaseManager.insertGame(game)

    val round =
      Round(
        taker = players[0],
        contract = Contract.GARDE,
        partner = null,
        oudlerCount = 2,
        takerPoints = 48,
        poignee = Poignee.NONE,
        petitAuBout = PetitAuBout.NONE,
        chelem = Chelem.NONE,
        index = 0,
      )

    databaseManager.addRound(game.id, round)

    val updatedRound = round.copy(takerPoints = 50)
    databaseManager.updateRound(updatedRound)

    val updatedGame = databaseManager.getGame(game.id)
    assertNotNull(updatedGame)
    assertEquals(1, updatedGame.rounds.size)
    assertEquals(50, updatedGame.rounds.first().takerPoints)
  }
}
