package proj.tarotmeter.axl.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

/**
 * Web-specific implementation of the navigation controller. The navigation-compose library version
 * 2.9.0 automatically integrates with the browser's History API on web platforms, allowing the URL
 * to reflect the current route and enabling browser back/forward navigation.
 *
 * This implementation simply delegates to the standard rememberNavController, which on web
 * platforms creates a NavController that:
 * - Updates the browser URL when navigating to a new route
 * - Responds to browser back/forward button clicks
 * - Maintains navigation state in sync with browser history
 *
 * @return A NavHostController configured for browser history integration
 */
@Composable
actual fun rememberPlatformNavController(): NavHostController = createDefaultNavController()
