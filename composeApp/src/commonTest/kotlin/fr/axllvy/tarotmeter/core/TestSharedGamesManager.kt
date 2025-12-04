package fr.axllvy.tarotmeter.core

import fr.axllvy.tarotmeter.core.data.cloud.CloudDatabaseManager
import fr.axllvy.tarotmeter.core.data.cloud.SharedGamesManager
import fr.axllvy.tarotmeter.core.data.cloud.auth.AuthManager
import fr.axllvy.tarotmeter.core.data.model.Game
import fr.axllvy.tarotmeter.core.data.model.Player
import fr.axllvy.tarotmeter.core.data.model.Round
import fr.axllvy.tarotmeter.core.data.model.enums.Chelem
import fr.axllvy.tarotmeter.core.data.model.enums.Contract
import fr.axllvy.tarotmeter.core.data.model.enums.PetitAuBout
import fr.axllvy.tarotmeter.core.data.model.enums.Poignee
import fr.axllvy.tarotmeter.util.TEST_TIMEOUT
import fr.axllvy.tarotmeter.util.TestWithKoin
import fr.axllvy.tarotmeter.util.generated.Secrets
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

class TestSharedGamesManager : TestWithKoin {
  private val authManager: AuthManager by inject()
  private val cloudDb: CloudDatabaseManager by inject()
  private val sharedGamesManager: SharedGamesManager by inject()

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

    val invitationCode = sharedGamesManager.createGameInvitation(game.id)
    assertTrue(invitationCode > 0, "Invitation code should be positive")

    // Disconnect user 1
    authManager.signOut()
    eventually(TEST_TIMEOUT) { assertNull(authManager.user) }

    // === USER 2: Join game and fetch it ===
    signInAsUser2()

    sharedGamesManager.joinGame(invitationCode)

    // Fetch non-owned games for user 2
    val nonOwnedGames = sharedGamesManager.getNonOwnedGames()

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

    val invitationCode = sharedGamesManager.createGameInvitation(game.id)

    // Disconnect user 1
    authManager.signOut()
    eventually(TEST_TIMEOUT) { assertNull(authManager.user) }

    // === USER 2: Join game and upsert a round ===
    signInAsUser2()

    sharedGamesManager.joinGame(invitationCode)

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

    sharedGamesManager.upsertRound(gameId, newRound)

    run { // Fetch the game and verify the round was inserted
      val nonOwnedGames = sharedGamesManager.getNonOwnedGames()
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
      sharedGamesManager.upsertRound(gameId, updatedRound)

      // Fetch the game and verify the round was inserted
      val nonOwnedGames = sharedGamesManager.getNonOwnedGames()
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

    val invitationCode = sharedGamesManager.createGameInvitation(game.id)

    // Disconnect user 1
    authManager.signOut()
    eventually(TEST_TIMEOUT) { assertNull(authManager.user) }

    // === USER 2: Join game and delete the round ===
    signInAsUser2()

    sharedGamesManager.joinGame(invitationCode)

    // Verify round exists before deletion
    var nonOwnedGames = sharedGamesManager.getNonOwnedGames()
    var sharedGame = nonOwnedGames.find { it.id == gameId }
    assertNotNull(sharedGame, "Game should be accessible")
    assertEquals(1, sharedGame.rounds.size)

    // Delete the round
    sharedGamesManager.deleteRound(roundId)

    // Verify round was deleted
    nonOwnedGames = sharedGamesManager.getNonOwnedGames()
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

    sharedGamesManager.createGameInvitation(game.id)

    // Disconnect user 1
    authManager.signOut()
    eventually(TEST_TIMEOUT) { assertNull(authManager.user) }

    // === USER 2: Sign in without joining the game ===
    signInAsUser2()

    // User 2 should NOT see the game since they didn't join
    val nonOwnedGames = sharedGamesManager.getNonOwnedGames()
    val sharedGame = nonOwnedGames.find { it.id == gameId }

    assertNull(sharedGame, "User 2 should not be able to see the game without an invitation")

    // Disconnect user 2
    authManager.signOut()
    eventually(TEST_TIMEOUT) { assertNull(authManager.user) }
  }
}
