package proj.tarotmeter.axl.core

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.koin.core.component.inject
import proj.tarotmeter.axl.core.data.DatabaseManager
import proj.tarotmeter.axl.core.data.model.Player
import proj.tarotmeter.axl.core.data.model.Round
import proj.tarotmeter.axl.core.data.model.enums.Chelem
import proj.tarotmeter.axl.core.data.model.enums.Contract
import proj.tarotmeter.axl.core.data.model.enums.PetitAuBout
import proj.tarotmeter.axl.core.data.model.enums.Poignee
import proj.tarotmeter.axl.core.provider.DataProvider
import proj.tarotmeter.axl.util.TestWithKoin

class TestGamesProvider : TestWithKoin {
  private val dataProvider: DataProvider by inject()
  private val databaseManager: DatabaseManager by inject()

  @AfterTest
  override fun tearDown() {
    runTest {
      databaseManager.clear()
      assertTrue(databaseManager.getPlayers().isEmpty())
      assertTrue(databaseManager.getGames().isEmpty())
    }
    super.tearDown()
  }

  private suspend fun DatabaseManager.clear() {
    val players = this.getPlayers()
    players.forEach { deletePlayer(it.id) }
  }

  @Test
  fun testCreateGameWithValidPlayers() = runTest {
    val players = setOf(Player("Alice"), Player("Bob"), Player("Charlie"))

    val game = dataProvider.createGame(players, "Test Game")

    assertNotNull(game)
    assertEquals(3, game.players.size)
    assertTrue(game.players.map { it.name }.containsAll(listOf("Alice", "Bob", "Charlie")))
    assertTrue(game.rounds.isEmpty())
  }

  @Test
  fun testCreateGameWithFourPlayers() = runTest {
    val players = setOf(Player("Player1"), Player("Player2"), Player("Player3"), Player("Player4"))

    val game = dataProvider.createGame(players, "Test Game")

    assertNotNull(game)
    assertEquals(4, game.players.size)
  }

  @Test
  fun testCreateGameWithFivePlayers() = runTest {
    val players =
      setOf(
        Player("Player1"),
        Player("Player2"),
        Player("Player3"),
        Player("Player4"),
        Player("Player5"),
      )

    val game = dataProvider.createGame(players, "Test Game")

    assertNotNull(game)
    assertEquals(5, game.players.size)
  }

  @Test
  fun testCreateGameWithTooFewPlayers() = runTest {
    val players = setOf(Player("Alice"), Player("Bob"))

    assertFailsWith<IllegalArgumentException> { dataProvider.createGame(players, "Test Game") }
  }

  @Test
  fun testCreateGameWithTooManyPlayers() = runTest {
    val players =
      setOf(
        Player("Player1"),
        Player("Player2"),
        Player("Player3"),
        Player("Player4"),
        Player("Player5"),
        Player("Player6"),
      )

    assertFailsWith<IllegalArgumentException> { dataProvider.createGame(players, "Test Game") }
  }

  @Test
  fun testGetGameById() = runTest {
    val players = setOf(Player("Alice"), Player("Bob"), Player("Charlie"))
    val createdGame = dataProvider.createGame(players, "Test Game")

    val retrievedGame = dataProvider.getGame(createdGame.id)

    assertNotNull(retrievedGame)
    assertEquals(createdGame.id, retrievedGame.id)
    assertEquals(3, retrievedGame.players.size)
  }

  @Test
  fun testGetNonExistentGame() = runTest {
    val nonExistentId = kotlin.uuid.Uuid.random()

    val retrievedGame = dataProvider.getGame(nonExistentId)

    assertNull(retrievedGame)
  }

  @Test
  fun testGetAllGames() = runTest {
    val players1 = setOf(Player("Alice"), Player("Bob"), Player("Charlie"))
    val players2 = setOf(Player("Dave"), Player("Eve"), Player("Frank"))

    val game1 = dataProvider.createGame(players1, "Test Game")
    val game2 = dataProvider.createGame(players2, "Test Game")

    val allGames = dataProvider.getGames()

    assertTrue(allGames.size >= 2)
    assertTrue(allGames.any { it.id == game1.id })
    assertTrue(allGames.any { it.id == game2.id })
  }

  @Test
  fun testGetGamesReturnsEmptyListWhenNoGames() = runTest {
    val games = dataProvider.getGames()

    assertTrue(games.isEmpty())
  }

  @Test
  fun testAddRoundToGame() = runTest {
    val players = setOf(Player("Alice"), Player("Bob"), Player("Charlie"))
    val game = dataProvider.createGame(players, "Test Game")

    val round =
      Round(
        taker = game.players[0],
        partner = null,
        contract = Contract.GARDE,
        oudlerCount = 2,
        takerPoints = 55,
        poignee = Poignee.NONE,
        petitAuBout = PetitAuBout.NONE,
        chelem = Chelem.NONE,
        index = 0,
      )

    dataProvider.addRound(game.id, round)

    val updatedGame = dataProvider.getGame(game.id)
    assertNotNull(updatedGame)
    assertEquals(1, updatedGame.rounds.size)
    assertEquals(round.id, updatedGame.rounds[0].id)
    assertEquals(Contract.GARDE, updatedGame.rounds[0].contract)
  }

  @Test
  fun testAddMultipleRoundsToGame() = runTest {
    val players = setOf(Player("Alice"), Player("Bob"), Player("Charlie"), Player("Dave"))
    val game = dataProvider.createGame(players, "Test Game")

    val round1 =
      Round(
        taker = game.players[0],
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
        taker = game.players[1],
        partner = null,
        contract = Contract.GARDE_SANS,
        oudlerCount = 3,
        takerPoints = 60,
        poignee = Poignee.SIMPLE,
        petitAuBout = PetitAuBout.TAKER,
        chelem = Chelem.NONE,
        index = 1,
      )

    dataProvider.addRound(game.id, round1)
    dataProvider.addRound(game.id, round2)

    val updatedGame = dataProvider.getGame(game.id)
    assertNotNull(updatedGame)
    assertEquals(2, updatedGame.rounds.size)
    assertEquals(round1.id, updatedGame.rounds[0].id)
    assertEquals(round2.id, updatedGame.rounds[1].id)
  }

  @Test
  fun testAddRoundWithPartner() = runTest {
    val players =
      setOf(
        Player("Player1"),
        Player("Player2"),
        Player("Player3"),
        Player("Player4"),
        Player("Player5"),
      )
    val game = dataProvider.createGame(players, "Test Game")

    val round =
      Round(
        taker = game.players[0],
        partner = game.players[1],
        contract = Contract.GARDE_CONTRE,
        oudlerCount = 2,
        takerPoints = 50,
        poignee = Poignee.DOUBLE,
        petitAuBout = PetitAuBout.DEFENSE,
        chelem = Chelem.NONE,
        index = 0,
      )

    dataProvider.addRound(game.id, round)

    val updatedGame = dataProvider.getGame(game.id)
    assertNotNull(updatedGame)
    assertEquals(1, updatedGame.rounds.size)

    val addedRound = updatedGame.rounds[0]
    assertEquals(game.players[0].id, addedRound.taker.id)
    assertNotNull(addedRound.partner)
    assertEquals(game.players[1].id, addedRound.partner.id)
  }

  @Test
  fun testAddRoundToNonExistentGame() = runTest {
    val nonExistentId = kotlin.uuid.Uuid.random()
    val players = setOf(Player("Alice"), Player("Bob"), Player("Charlie"))
    val game = dataProvider.createGame(players, "Test Game")

    val round =
      Round(
        taker = game.players[0],
        partner = null,
        contract = Contract.PETITE,
        oudlerCount = 1,
        takerPoints = 40,
        poignee = Poignee.NONE,
        petitAuBout = PetitAuBout.NONE,
        chelem = Chelem.NONE,
        index = 0,
      )

    dataProvider.addRound(nonExistentId, round)

    val retrievedGame = dataProvider.getGame(nonExistentId)
    assertNull(retrievedGame)
  }

  @Test
  fun testRoundIndexIncrements() = runTest {
    val players = setOf(Player("Alice"), Player("Bob"), Player("Charlie"))
    val game = dataProvider.createGame(players, "Test Game")

    val round1 =
      Round(
        taker = game.players[0],
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
        taker = game.players[1],
        partner = null,
        contract = Contract.GARDE,
        oudlerCount = 2,
        takerPoints = 50,
        poignee = Poignee.NONE,
        petitAuBout = PetitAuBout.NONE,
        chelem = Chelem.NONE,
        index = 1,
      )

    val round3 =
      Round(
        taker = game.players[2],
        partner = null,
        contract = Contract.GARDE_SANS,
        oudlerCount = 3,
        takerPoints = 60,
        poignee = Poignee.NONE,
        petitAuBout = PetitAuBout.NONE,
        chelem = Chelem.NONE,
        index = 2,
      )

    dataProvider.addRound(game.id, round1)
    dataProvider.addRound(game.id, round2)
    dataProvider.addRound(game.id, round3)

    val updatedGame = dataProvider.getGame(game.id)
    assertNotNull(updatedGame)
    assertEquals(3, updatedGame.rounds.size)
    assertEquals(0, updatedGame.rounds[0].index)
    assertEquals(1, updatedGame.rounds[1].index)
    assertEquals(2, updatedGame.rounds[2].index)
  }
}
