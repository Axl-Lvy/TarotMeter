package proj.tarotmeter.axl.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

/**
 * Creates a navigation controller with platform-specific configuration. On web platforms, this
 * ensures proper integration with browser history and URL synchronization.
 *
 * @return A configured NavHostController instance
 */
@Composable
expect fun rememberPlatformNavController(): NavHostController

/**
 * Default implementation that simply delegates to the standard rememberNavController. Web platform
 * will provide an enhanced implementation with browser history integration.
 */
@Composable
internal fun createDefaultNavController(): NavHostController = rememberNavController()
