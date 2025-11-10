package proj.tarotmeter.axl.core

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.koin.core.component.inject
import proj.tarotmeter.axl.core.data.DatabaseManager
import proj.tarotmeter.axl.core.data.model.Game
import proj.tarotmeter.axl.core.data.model.Player
import proj.tarotmeter.axl.core.data.model.Round
import proj.tarotmeter.axl.core.data.model.enums.Chelem
import proj.tarotmeter.axl.core.data.model.enums.Contract
import proj.tarotmeter.axl.core.data.model.enums.PetitAuBout
import proj.tarotmeter.axl.core.data.model.enums.Poignee
import proj.tarotmeter.axl.util.TestWithKoin

class TestDatabaseManager : TestWithKoin {
  private val dbManager: DatabaseManager by inject()

  @AfterTest
  fun cleanDb() = runTest {
    dbManager.clear()
    assertTrue(dbManager.getPlayers().isEmpty())
    assertTrue(dbManager.getGames().isEmpty())
  }

  private suspend fun DatabaseManager.clear() {
    val players = this.getPlayers()
    players.forEach { deletePlayer(it.id) }
  }

  @Test
  fun testInsertAndGetPlayers() = runTest {
    val player1 = Player("Alice")
    val player2 = Player("Bob")

    dbManager.insertPlayer(player1)
    dbManager.insertPlayer(player2)

    val players = dbManager.getPlayers()

    assertTrue(players.size >= 2)
    assertTrue(players.any { it.name == "Alice" })
    assertTrue(players.any { it.name == "Bob" })
  }

  @Test
  fun testRenamePlayer() = runTest {
    val player = Player("Charlie")
    dbManager.insertPlayer(player)

    dbManager.renamePlayer(player.id, "Charles")

    val players = dbManager.getPlayers()
    val renamedPlayer = players.find { it.id == player.id }

    assertNotNull(renamedPlayer)
    assertEquals("Charles", renamedPlayer.name)
  }

  @Test
  fun testDeletePlayer() = runTest {
    val player = Player("David")
    dbManager.insertPlayer(player)

    val playersBeforeDelete = dbManager.getPlayers()
    val playerExists = playersBeforeDelete.any { it.id == player.id }
    assertTrue(playerExists)

    dbManager.deletePlayer(player.id)

    val playersAfterDelete = dbManager.getPlayers()
    val playerStillExists = playersAfterDelete.any { it.id == player.id }
    assertTrue(!playerStillExists)
  }

  @Test
  fun testInsertAndGetGames() = runTest {
    val players = listOf(Player("Player1"), Player("Player2"), Player("Player3"))

    players.forEach { dbManager.insertPlayer(it) }

    val game = Game(players, name = "Test Game")
    dbManager.insertGame(game)

    val games = dbManager.getGames()
    assertTrue(games.isNotEmpty())

    val insertedGame = games.find { it.id == game.id }
    assertNotNull(insertedGame)
    assertEquals(3, insertedGame.players.size)
  }

  @Test
  fun testGetGameById() = runTest {
    val players = listOf(Player("Player1"), Player("Player2"), Player("Player3"), Player("Player4"))

    players.forEach { dbManager.insertPlayer(it) }

    val game = Game(players, name = "Test Game")
    dbManager.insertGame(game)

    val retrievedGame = dbManager.getGame(game.id)
    assertNotNull(retrievedGame)
    assertEquals(game.id, retrievedGame.id)
    assertEquals(4, retrievedGame.players.size)
  }

  @Test
  fun testAddRoundToGame() = runTest {
    val players =
      listOf(
        Player("Player1"),
        Player("Player2"),
        Player("Player3"),
        Player("Player4"),
        Player("Player5"),
      )

    players.forEach { dbManager.insertPlayer(it) }

    val game = Game(players, name = "Test Game")
    dbManager.insertGame(game)

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

    dbManager.addRound(game.id, round)

    val updatedGame = dbManager.getGame(game.id)
    assertNotNull(updatedGame)
    assertEquals(1, updatedGame.rounds.size)

    val addedRound = updatedGame.rounds.first()
    assertEquals(players[0].name, addedRound.taker.name)
    assertEquals(Contract.GARDE, addedRound.contract)
    assertEquals(2, addedRound.oudlerCount)
    assertEquals(48, addedRound.takerPoints)
  }

  @Test
  fun testAddMultipleRoundsToGame() = runTest {
    val players = listOf(Player("Player1"), Player("Player2"), Player("Player3"))

    players.forEach { dbManager.insertPlayer(it) }

    val game = Game(players, name = "Test Game")
    dbManager.insertGame(game)

    val round1 =
      Round(
        taker = players[0],
        contract = Contract.PETITE,
        partner = null,
        oudlerCount = 1,
        takerPoints = 40,
        poignee = Poignee.NONE,
        petitAuBout = PetitAuBout.NONE,
        index = 0,
        chelem = Chelem.NONE,
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
        index = 1,
        chelem = Chelem.ANNOUNCED,
      )

    dbManager.addRound(game.id, round1)
    dbManager.addRound(game.id, round2)

    val updatedGame = dbManager.getGame(game.id)
    assertNotNull(updatedGame)
    assertEquals(2, updatedGame.rounds.size)
  }

  @Test
  fun testRemoveGame() = runTest {
    val players = listOf(Player("Player1"), Player("Player2"), Player("Player3"))

    players.forEach { dbManager.insertPlayer(it) }

    val game = Game(players, name = "Test Game")
    dbManager.insertGame(game)

    val gamesBeforeRemoval = dbManager.getGames()
    assertTrue(gamesBeforeRemoval.any { it.id == game.id })

    dbManager.deleteGame(game.id)

    val gamesAfterRemoval = dbManager.getGames()
    assertTrue(gamesAfterRemoval.none { it.id == game.id })

    val removedGame = dbManager.getGame(game.id)
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

    players.forEach { dbManager.insertPlayer(it) }

    val game = Game(players, name = "Test Game")
    dbManager.insertGame(game)

    val round =
      Round(
        taker = players[2],
        contract = Contract.GARDE_CONTRE,
        partner = players[1],
        oudlerCount = 3,
        takerPoints = 70,
        index = 0,
        poignee = Poignee.TRIPLE,
        petitAuBout = PetitAuBout.DEFENSE,
        chelem = Chelem.FAILED,
      )

    dbManager.addRound(game.id, round)

    val updatedGame = dbManager.getGame(game.id)
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

    players.forEach { dbManager.insertPlayer(it) }

    val game = Game(players, name = "Test Game")
    dbManager.insertGame(game)

    val retrievedGame = dbManager.getGame(game.id)
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

    players.forEach { dbManager.insertPlayer(it) }

    val game = Game(players, name = "Test Game")
    dbManager.insertGame(game)

    val retrievedGame = dbManager.getGame(game.id)
    assertNotNull(retrievedGame)
    assertEquals(5, retrievedGame.players.size)
  }

  @Test
  fun testDeleteRound() = runTest {
    val players = listOf(Player("Player1"), Player("Player2"), Player("Player3"))
    players.forEach { dbManager.insertPlayer(it) }

    val game = Game(players, name = "Test Game")
    dbManager.insertGame(game)

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

    dbManager.addRound(game.id, round)
    dbManager.deleteRound(round.id)

    val updatedGame = dbManager.getGame(game.id)
    assertNotNull(updatedGame)
    assertTrue(updatedGame.rounds.isEmpty())
  }

  @Test
  fun testUpdateRound() = runTest {
    val players = listOf(Player("Player1"), Player("Player2"), Player("Player3"))
    players.forEach { dbManager.insertPlayer(it) }

    val game = Game(players, name = "Test Game")
    dbManager.insertGame(game)

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

    dbManager.addRound(game.id, round)

    val updatedRound = round.copy(takerPoints = 50)
    dbManager.updateRound(updatedRound)

    val updatedGame = dbManager.getGame(game.id)
    assertNotNull(updatedGame)
    assertEquals(1, updatedGame.rounds.size)
    assertEquals(50, updatedGame.rounds.first().takerPoints)
  }
}
