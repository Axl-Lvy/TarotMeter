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

class TestNonOwnedGamesManager : TestWithKoin {
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
}
