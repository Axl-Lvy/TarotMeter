package proj.tarotmeter.axl.util

import io.kotest.assertions.nondeterministic.eventually
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import org.koin.core.component.inject
import proj.tarotmeter.axl.core.data.cloud.auth.AuthManager
import proj.tarotmeter.axl.util.generated.Secrets

abstract class TestAuthenticated : TestWithKoin {
  private val authManager: AuthManager by inject()

  @BeforeTest
  override fun setUp() {
    super.setUp()
    ensureSignedOut()
    runTest {
      authManager.signInFromEmail(Secrets.testUserMail1, Secrets.testUserPassword)
      eventually(TEST_TIMEOUT) { assertNotNull(authManager.user) }
    }
  }

  @AfterTest
  override fun tearDown() {
    ensureSignedOut()
    super.tearDown()
  }

  /**
   * Ensures that the user is signed out. If a user is currently signed in, it signs them out and
   * waits until the sign-out process is complete.
   */
  fun ensureSignedOut() {
    runTest {
      if (authManager.user != null) {
        authManager.signOut()
        eventually(TEST_TIMEOUT) { assertNull(authManager.user) }
      }
    }
  }
}
