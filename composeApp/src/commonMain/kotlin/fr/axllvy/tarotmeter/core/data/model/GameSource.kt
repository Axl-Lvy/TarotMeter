package fr.axllvy.tarotmeter.core.data.model

/**
 * Represents the source/storage location of a game.
 *
 * Games can be stored locally on the device or remotely (shared with other users).
 *
 * Note: "REMOTE" games are also referred to as "Shared Games" or "Cross-User Games" in the UI, as
 * they are games owned by other users that the current user has joined.
 */
enum class GameSource {
  /** Game stored locally in the device's database */
  LOCAL,

  /**
   * Game stored remotely and shared with other users (not owned by current user). Also known as
   * "Shared Games" or "Cross-User Games" in the UI.
   */
  REMOTE,
}
