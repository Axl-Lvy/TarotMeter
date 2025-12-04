package fr.tarotmeter.ui.navigation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a navigation route within the application. This sealed interface defines the structure
 * for all routes in the app and ensures consistent navigation behavior across platforms.
 */
sealed interface Route {
  /** The unique route path used for navigation. */
  val route: String

  /** The human-readable title for this route, displayed in the app bar. */
  val title: String

  /** Home screen route - the starting point of the application. */
  data object Home : Route {
    override val route: String = "home"
    override val title: String = "Tarot Meter"
  }

  /**
   * Players management screen route. This screen allows users to view, add, edit, and delete
   * players.
   */
  data object Players : Route {
    override val route: String = "players"
    override val title: String = "Players"
  }

  /** Settings screen route. This screen provides app configuration options. */
  data object Settings : Route {
    override val route: String = "settings"
    override val title: String = "Settings"
  }

  /**
   * New Game creation screen route. This screen allows users to set up a new game with selected
   * players.
   */
  data object NewGame : Route {
    override val route: String = "new"
    override val title: String = "New Game"
  }

  /** Game History screen route. This screen shows a list of past games. */
  data object History : Route {
    override val route: String = "history"
    override val title: String = "Game History"
  }

  /**
   * Game editor screen route with a specific game ID. This screen allows viewing and editing an
   * existing game.
   *
   * @property id The unique identifier of the game to edit
   */
  @Serializable
  @SerialName("game")
  data class Game(val id: String) : Route {
    override val route: String
      get() = ROUTE

    override val title: String
      get() = TITLE

    companion object {
      /** The base route path for game editing. */
      const val ROUTE = "game"

      /** The title displayed when editing a game. */
      const val TITLE = "Game Editor"
    }
  }

  /**
   * Email confirmation screen route with a token hash. This screen verifies the user's email
   * address.
   *
   * @property tokenHash The token hash received from the verification email
   */
  @Serializable
  @SerialName("confirm-email")
  data class ConfirmEmail(val tokenHash: String) : Route {
    override val route: String
      get() = ROUTE

    override val title: String
      get() = TITLE

    companion object {
      /** The base route path for email confirmation. */
      const val ROUTE = "confirm-email"

      /** The title displayed during email confirmation. */
      const val TITLE = "Confirm Email"
    }
  }

  /**
   * Join game screen route with an invitation code. This screen joins a game using the provided
   * code.
   *
   * @property invitationCode The 8-digit invitation code from the QR code or deep link
   */
  @Serializable
  @SerialName("join")
  data class JoinGame(val invitationCode: String) : Route {
    override val route: String
      get() = ROUTE

    override val title: String
      get() = TITLE

    companion object {
      /** The base route path for joining a game. */
      const val ROUTE = "join"

      /** The title displayed when joining a game. */
      const val TITLE = "Join Game"
    }
  }
}
