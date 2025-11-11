package proj.tarotmeter.axl.core

import io.kotest.assertions.nondeterministic.eventually
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.koin.core.component.inject
import proj.tarotmeter.axl.core.data.cloud.CloudDatabaseManager
import proj.tarotmeter.axl.core.data.cloud.ForeignerGamesManager
import proj.tarotmeter.axl.core.data.cloud.auth.AuthManager
import proj.tarotmeter.axl.core.data.model.Game
import proj.tarotmeter.axl.core.data.model.Player
import proj.tarotmeter.axl.core.data.model.Round
import proj.tarotmeter.axl.core.data.model.enums.Chelem
import proj.tarotmeter.axl.core.data.model.enums.Contract
import proj.tarotmeter.axl.core.data.model.enums.PetitAuBout
import proj.tarotmeter.axl.core.data.model.enums.Poignee
import proj.tarotmeter.axl.util.TEST_TIMEOUT
import proj.tarotmeter.axl.util.TestWithKoin
import proj.tarotmeter.axl.util.generated.Secrets

class TestSharedGamesManager : TestWithKoin {
  private val authManager: AuthManager by inject()
  private val cloudDb: CloudDatabaseManager by inject()
  private val foreignerGamesManager: ForeignerGamesManager by inject()

  @BeforeTest
  override fun setUp() {
    super.setUp()
    ensureSignedOut()
  }

  @AfterTest
  override fun tearDown() {
    ensureSignedOut()
    runTest { cleanUpUser1Data() }
    super.tearDown()
  }

  private fun ensureSignedOut() {
    runTest {
      if (authManager.user != null) {
        authManager.signOut()
        eventually(TEST_TIMEOUT) { assertNull(authManager.user) }
      }
    }
  }

  private suspend fun signInAsUser1() {
    authManager.signInFromEmail(Secrets.testUserMail1, Secrets.testUserPassword)
    eventually(TEST_TIMEOUT) { assertNotNull(authManager.user) }
  }

  private suspend fun signInAsUser2() {
    authManager.signInFromEmail(Secrets.testUserMail2, Secrets.testUserPassword)
    eventually(TEST_TIMEOUT) { assertNotNull(authManager.user) }
  }

  private suspend fun cleanUpUser1Data() {
    signInAsUser1()
    cloudDb.hardDeleteGames()
    cloudDb.hardDeletePlayers()
    authManager.signOut()
    eventually(TEST_TIMEOUT) { assertNull(authManager.user) }
  }

  @Test
  fun `test game invitation and cross-user access`() = runTest {
    // === USER 1: Create game, add round, create invitation ===
    signInAsUser1()

    val players = listOf(Player("Alice"), Player("Bob"), Player("Charlie"))
    players.forEach { cloudDb.insertPlayer(it) }

    val game = Game(players, name = "Shared Game")
    cloudDb.insertGame(game)
    val gameId = game.id

    val round =
      Round(
        taker = players[0],
        contract = Contract.GARDE,
        partner = null,
        oudlerCount = 2,
        takerPoints = 48,
        poignee = Poignee.SIMPLE,
        petitAuBout = PetitAuBout.TAKER,
        chelem = Chelem.NONE,
        index = 0,
      )
    cloudDb.addRound(game.id, round)

    val invitationCode = foreignerGamesManager.createGameInvitation(game.id)
    assertTrue(invitationCode > 0, "Invitation code should be positive")

    // Disconnect user 1
    authManager.signOut()
    eventually(TEST_TIMEOUT) { assertNull(authManager.user) }

    // === USER 2: Join game and fetch it ===
    signInAsUser2()

    foreignerGamesManager.joinGame(invitationCode)

    // Fetch non-owned games for user 2
    val nonOwnedGames = foreignerGamesManager.getNonOwnedGames()

    // Assert that user 2 can see the game
    val sharedGame = nonOwnedGames.find { it.id == gameId }
    assertNotNull(sharedGame, "User 2 should be able to see the shared game")
    assertEquals("Shared Game", sharedGame.name)
    assertEquals(3, sharedGame.players.size)
    assertEquals(1, sharedGame.rounds.size)

    val fetchedRound = sharedGame.rounds.first()
    assertEquals(Contract.GARDE, fetchedRound.contract)
    assertEquals(2, fetchedRound.oudlerCount)
    assertEquals(48, fetchedRound.takerPoints)
    assertEquals(Poignee.SIMPLE, fetchedRound.poignee)

    // Disconnect user 2
    authManager.signOut()
    eventually(TEST_TIMEOUT) { assertNull(authManager.user) }
  }

  @Test
  fun `test upsertRound in cross-user game`() = runTest {
    // === USER 1: Create game and create invitation ===
    signInAsUser1()

    val players = listOf(Player("Alice"), Player("Bob"), Player("Charlie"))
    players.forEach { cloudDb.insertPlayer(it) }

    val game = Game(players, name = "Upsert Test Game")
    cloudDb.insertGame(game)
    val gameId = game.id

    val invitationCode = foreignerGamesManager.createGameInvitation(game.id)

    // Disconnect user 1
    authManager.signOut()
    eventually(TEST_TIMEOUT) { assertNull(authManager.user) }

    // === USER 2: Join game and upsert a round ===
    signInAsUser2()

    foreignerGamesManager.joinGame(invitationCode)

    val newRound =
      Round(
        taker = players[0],
        contract = Contract.PETITE,
        partner = null,
        oudlerCount = 1,
        takerPoints = 60,
        poignee = Poignee.DOUBLE,
        petitAuBout = PetitAuBout.DEFENSE,
        chelem = Chelem.NONE,
        index = 0,
      )

    foreignerGamesManager.upsertRound(gameId, newRound)

    run { // Fetch the game and verify the round was inserted
      val nonOwnedGames = foreignerGamesManager.getNonOwnedGames()
      val sharedGame = nonOwnedGames.find { it.id == gameId }

      assertNotNull(sharedGame, "Game should be accessible")
      assertEquals(1, sharedGame.rounds.size)

      val upsertedRound = sharedGame.rounds.first()
      assertEquals(Contract.PETITE, upsertedRound.contract)
      assertEquals(1, upsertedRound.oudlerCount)
      assertEquals(60, upsertedRound.takerPoints)
      assertEquals(Poignee.DOUBLE, upsertedRound.poignee)
      assertEquals(PetitAuBout.DEFENSE, upsertedRound.petitAuBout)
    }

    val updatedRound = newRound.copy(takerPoints = 70)
    run {
      foreignerGamesManager.upsertRound(gameId, updatedRound)

      // Fetch the game and verify the round was inserted
      val nonOwnedGames = foreignerGamesManager.getNonOwnedGames()
      val sharedGame = nonOwnedGames.find { it.id == gameId }

      assertNotNull(sharedGame, "Game should be accessible")
      assertEquals(1, sharedGame.rounds.size)

      val upsertedRound = sharedGame.rounds.first()
      assertEquals(70, upsertedRound.takerPoints)
    }

    // Disconnect user 2
    authManager.signOut()
    eventually(TEST_TIMEOUT) { assertNull(authManager.user) }
  }

  @Test
  fun `test deleteRound in cross-user game`() = runTest {
    // === USER 1: Create game with a round and create invitation ===
    signInAsUser1()

    val players = listOf(Player("Alice"), Player("Bob"), Player("Charlie"))
    players.forEach { cloudDb.insertPlayer(it) }

    val game = Game(players, name = "Delete Test Game")
    val round =
      Round(
        taker = players[0],
        contract = Contract.GARDE_SANS,
        partner = null,
        oudlerCount = 3,
        takerPoints = 75,
        poignee = Poignee.TRIPLE,
        petitAuBout = PetitAuBout.TAKER,
        chelem = Chelem.NONE,
        index = 0,
      )
    game.addRound(round)
    cloudDb.insertGame(game)
    val gameId = game.id
    val roundId = game.rounds.first().id

    val invitationCode = foreignerGamesManager.createGameInvitation(game.id)

    // Disconnect user 1
    authManager.signOut()
    eventually(TEST_TIMEOUT) { assertNull(authManager.user) }

    // === USER 2: Join game and delete the round ===
    signInAsUser2()

    foreignerGamesManager.joinGame(invitationCode)

    // Verify round exists before deletion
    var nonOwnedGames = foreignerGamesManager.getNonOwnedGames()
    var sharedGame = nonOwnedGames.find { it.id == gameId }
    assertNotNull(sharedGame, "Game should be accessible")
    assertEquals(1, sharedGame.rounds.size)

    // Delete the round
    foreignerGamesManager.deleteRound(roundId)

    // Verify round was deleted
    nonOwnedGames = foreignerGamesManager.getNonOwnedGames()
    sharedGame = nonOwnedGames.find { it.id == gameId }
    assertNotNull(sharedGame, "Game should still be accessible")
    assertTrue(sharedGame.rounds.isEmpty(), "Round should be deleted")

    // Disconnect user 2
    authManager.signOut()
    eventually(TEST_TIMEOUT) { assertNull(authManager.user) }
  }

  @Test
  fun `test non-invited user cannot see shared game`() = runTest {
    // === USER 1: Create game and create invitation ===
    signInAsUser1()

    val players = listOf(Player("Alice"), Player("Bob"), Player("Charlie"))
    players.forEach { cloudDb.insertPlayer(it) }

    val game = Game(players, name = "Private Game")
    cloudDb.insertGame(game)
    val gameId = game.id

    val round =
      Round(
        taker = players[0],
        contract = Contract.GARDE,
        partner = null,
        oudlerCount = 2,
        takerPoints = 48,
        poignee = Poignee.SIMPLE,
        petitAuBout = PetitAuBout.TAKER,
        chelem = Chelem.NONE,
        index = 0,
      )
    cloudDb.addRound(game.id, round)

    foreignerGamesManager.createGameInvitation(game.id)

    // Disconnect user 1
    authManager.signOut()
    eventually(TEST_TIMEOUT) { assertNull(authManager.user) }

    // === USER 2: Sign in without joining the game ===
    signInAsUser2()

    // User 2 should NOT see the game since they didn't join
    val nonOwnedGames = foreignerGamesManager.getNonOwnedGames()
    val sharedGame = nonOwnedGames.find { it.id == gameId }

    assertNull(sharedGame, "User 2 should not be able to see the game without an invitation")

    // Disconnect user 2
    authManager.signOut()
    eventually(TEST_TIMEOUT) { assertNull(authManager.user) }
  }
}
