package proj.tarotmeter.axl.core

import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.status.SessionStatus
import io.kotest.assertions.nondeterministic.eventually
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.koin.core.component.inject
import proj.tarotmeter.axl.core.data.cloud.auth.AuthManager
import proj.tarotmeter.axl.core.data.config.AUTH_ACCESS_TOKEN
import proj.tarotmeter.axl.core.data.config.AUTH_REFRESH_TOKEN
import proj.tarotmeter.axl.core.data.config.KEEP_LOGGED_IN
import proj.tarotmeter.axl.util.TEST_TIMEOUT
import proj.tarotmeter.axl.util.TestWithKoin
import proj.tarotmeter.axl.util.generated.Secrets

/**
 * Test suite for authentication functionality.
 *
 * Tests cover sign in, sign out, session management, token persistence, and error handling
 * scenarios.
 */
class TestAuthentication : TestWithKoin {

  private val authManager: AuthManager by inject()

  @BeforeTest
  override fun setUp() {
    super.setUp()
    // Ensure a clean state before each test
    AUTH_REFRESH_TOKEN.reset()
    AUTH_ACCESS_TOKEN.reset()
    KEEP_LOGGED_IN.reset()
    runTest {
      authManager.signOut()
      eventually(duration = TEST_TIMEOUT) { assertNull(authManager.user) }
    }
  }

  @AfterTest
  override fun tearDown() {
    // Clean up after each test
    runTest { authManager.signOut() }

    super.tearDown()
  }

  @Test
  fun testSuccessfulSignIn() = runTest {
    authManager.signInFromEmail(
      providedEmail = Secrets.testUserMail,
      providedPassword = Secrets.testUserPassword,
    )

    eventually(duration = TEST_TIMEOUT) {
      assertNotNull(authManager.user, "User should not be null after successful sign in")
      assertEquals(Secrets.testUserMail, authManager.user?.email, "Email should match")
    }
  }

  @Test
  fun testInvalidCredentials() = runTest {
    // Clear any existing session
    if (authManager.user != null) {
      authManager.signOut()
    }

    var exceptionThrown = false
    try {
      authManager.signInFromEmail(
        providedEmail = "invalid@email.com",
        providedPassword = "wrongpassword",
      )
    } catch (_: AuthRestException) {
      exceptionThrown = true
    }

    assertTrue(exceptionThrown, "AuthRestException should be thrown for invalid credentials")
    assertNull(authManager.user, "User should remain null after failed sign in")
  }

  @Test
  fun testSignOut() = runTest {
    // Sign in first
    authManager.signInFromEmail(
      providedEmail = Secrets.testUserMail,
      providedPassword = Secrets.testUserPassword,
    )

    eventually(duration = TEST_TIMEOUT) {
      assertNotNull(authManager.user, "User should not be null after sign in")
    }

    authManager.signOut()

    eventually(duration = TEST_TIMEOUT) {
      assertNull(authManager.user, "User should be null after sign out")
    }
  }

  @Test
  fun testTokenPersistenceWhenKeepLoggedInEnabled() = runTest {
    // Enable keep logged in
    KEEP_LOGGED_IN.value = true

    // Sign in
    authManager.signInFromEmail(
      providedEmail = Secrets.testUserMail,
      providedPassword = Secrets.testUserPassword,
    )

    eventually(duration = TEST_TIMEOUT) {
      // Tokens should be saved when keep logged in is enabled
      assertTrue(AUTH_REFRESH_TOKEN.value.isNotEmpty(), "Refresh token should be saved")
      assertTrue(AUTH_ACCESS_TOKEN.value.isNotEmpty(), "Access token should be saved")
    }
  }

  @Test
  fun testTokensClearedWhenKeepLoggedInDisabled() = runTest {
    // Disable keep logged in
    KEEP_LOGGED_IN.value = false

    // Sign in
    authManager.signInFromEmail(
      providedEmail = Secrets.testUserMail,
      providedPassword = Secrets.testUserPassword,
    )

    eventually(duration = TEST_TIMEOUT) {
      // Tokens should not be saved when keep logged in is disabled
      assertTrue(AUTH_REFRESH_TOKEN.value.isEmpty(), "Refresh token should not be saved")
      assertTrue(AUTH_ACCESS_TOKEN.value.isEmpty(), "Access token should not be saved")
    }
  }

  @Test
  fun testTokensClearedOnSignOut() = runTest {
    // Enable keep logged in and sign in
    KEEP_LOGGED_IN.value = true
    authManager.signInFromEmail(
      providedEmail = Secrets.testUserMail,
      providedPassword = Secrets.testUserPassword,
    )

    eventually(duration = TEST_TIMEOUT) {
      // Verify tokens are saved
      assertTrue(AUTH_REFRESH_TOKEN.value.isNotEmpty(), "Refresh token should be saved")
      assertTrue(AUTH_ACCESS_TOKEN.value.isNotEmpty(), "Access token should be saved")
    }

    // Sign out
    authManager.signOut()

    eventually(duration = TEST_TIMEOUT) {
      // Tokens should be cleared after sign out
      assertTrue(
        AUTH_REFRESH_TOKEN.value.isEmpty(),
        "Refresh token should be cleared after sign out",
      )
      assertTrue(AUTH_ACCESS_TOKEN.value.isEmpty(), "Access token should be cleared after sign out")
      assertFalse(KEEP_LOGGED_IN.value, "Keep logged in should be reset after sign out")
    }
  }

  @Test
  fun testSessionStatusListener() = runTest {
    var sessionStatusReceived: SessionStatus? = null

    // Register listener
    authManager.registerListener { status -> sessionStatusReceived = status }

    // Sign in to trigger listener
    authManager.signInFromEmail(
      providedEmail = Secrets.testUserMail,
      providedPassword = Secrets.testUserPassword,
    )

    eventually(duration = TEST_TIMEOUT) {
      assertNotNull(sessionStatusReceived, "Session status should be received by listener")
      assertTrue(
        sessionStatusReceived is SessionStatus.Authenticated,
        "Session status should be Authenticated after sign in",
      )
    }

    // Test sign out listener
    authManager.signOut()

    eventually(duration = TEST_TIMEOUT) {
      assertTrue(
        sessionStatusReceived is SessionStatus.NotAuthenticated,
        "Session status should be NotAuthenticated after sign out",
      )
    }
  }

  @Test
  fun testEmptyEmailValidation() = runTest {
    var exceptionThrown = false
    try {
      authManager.signInFromEmail(providedEmail = "", providedPassword = Secrets.testUserPassword)
    } catch (_: Exception) {
      exceptionThrown = true
    }

    eventually(duration = TEST_TIMEOUT) {
      assertTrue(exceptionThrown, "Exception should be thrown for empty email")
      assertNull(authManager.user, "User should remain null after failed sign in")
    }
  }

  @Test
  fun testEmptyPasswordValidation() = runTest {
    var exceptionThrown = false
    try {
      authManager.signInFromEmail(providedEmail = Secrets.testUserMail, providedPassword = "")
    } catch (_: Exception) {
      exceptionThrown = true
    }

    eventually(duration = TEST_TIMEOUT) {
      assertTrue(exceptionThrown, "Exception should be thrown for empty password")
      assertNull(authManager.user, "User should remain null after failed sign in")
    }
  }
}
