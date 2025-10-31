package proj.tarotmeter.axl.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

/**
 * iOS-specific implementation of the navigation controller. Uses the standard
 * rememberNavController implementation.
 */
@Composable
actual fun rememberPlatformNavController(): NavHostController = createDefaultNavController()
