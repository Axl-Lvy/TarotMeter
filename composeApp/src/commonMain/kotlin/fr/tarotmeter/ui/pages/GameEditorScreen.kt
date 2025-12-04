package fr.tarotmeter.ui.pages

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Regular
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.regular.Edit
import compose.icons.fontawesomeicons.solid.ChevronLeft
import compose.icons.fontawesomeicons.solid.Share
import compose.icons.fontawesomeicons.solid.Times
import fr.tarotmeter.core.data.model.Game
import fr.tarotmeter.core.data.model.GameSource
import fr.tarotmeter.core.data.model.Round
import fr.tarotmeter.core.data.model.calculated.Scores
import fr.tarotmeter.core.provider.DataProvider
import fr.tarotmeter.ui.components.CustomElevatedCard
import fr.tarotmeter.ui.components.EmptyState
import fr.tarotmeter.ui.components.GameInvitationDialog
import fr.tarotmeter.ui.components.GameModeToggle
import fr.tarotmeter.ui.components.GameRenameDialog
import fr.tarotmeter.ui.components.GameScreenTab
import fr.tarotmeter.ui.components.GameSourceBadge
import fr.tarotmeter.ui.components.PlayerAvatar
import fr.tarotmeter.ui.components.PlayerScoresRow
import fr.tarotmeter.ui.components.RoundEditor
import fr.tarotmeter.ui.components.ScoreText
import fr.tarotmeter.ui.pages.stats.GameStatsView
import kotlin.uuid.Uuid
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import tarotmeter.composeapp.generated.resources.Res
import tarotmeter.composeapp.generated.resources.cd_edit
import tarotmeter.composeapp.generated.resources.cd_navigate_back
import tarotmeter.composeapp.generated.resources.game_editor_delete_confirm_message
import tarotmeter.composeapp.generated.resources.game_editor_delete_confirm_title
import tarotmeter.composeapp.generated.resources.game_editor_edit
import tarotmeter.composeapp.generated.resources.game_editor_empty_state
import tarotmeter.composeapp.generated.resources.game_editor_invite
import tarotmeter.composeapp.generated.resources.game_editor_round_history
import tarotmeter.composeapp.generated.resources.game_editor_taker_points
import tarotmeter.composeapp.generated.resources.general_cancel
import tarotmeter.composeapp.generated.resources.general_delete
import tarotmeter.composeapp.generated.resources.general_with
import tarotmeter.composeapp.generated.resources.history_rename_game
import tarotmeter.composeapp.generated.resources.tarot_oudlers

/**
 * Screen for editing a specific game. Displays game scores, allows adding rounds, and shows round
 * history.
 *
 * @param gameId The ID of the game to edit
 */
@Composable
fun GameEditorScreen(gameId: Uuid, dataProvider: DataProvider = koinInject()) {
  val state = rememberGameEditorState(gameId, dataProvider)

  val currentGame = state.game
  if (currentGame == null) {
    LoadingState()
    return
  }

  PullToRefreshBox(isRefreshing = state.isRefreshing, onRefresh = { state.triggerRefresh() }) {
    Column(
      modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      GameModeToggle(
        selectedTab = state.selectedTab,
        onTabSelected = { state.selectedTab = it },
        modifier = Modifier.fillMaxWidth(),
      )

      Crossfade(
        targetState = state.selectedTab,
        modifier = Modifier.fillMaxSize(),
        label = "game-editor-mode",
      ) { mode ->
        when (mode) {
          GameScreenTab.EDIT_GAME -> GameEditingTabContent(state = state)
          GameScreenTab.STATS -> StatsTabContent(game = currentGame)
        }
      }
    }

    GameEditorDialogs(state = state, gameId = gameId)
  }
}

/**
 * State holder for the GameEditorScreen, managing game data and UI state.
 *
 * @param gameId The ID of the game being edited
 * @param dataProvider The data provider for fetching and updating game data
 * @param game The current game data
 * @param editingRound The round currently being edited, if any
 * @param showDeleteDialog Whether to show the delete round confirmation dialog
 * @param roundToDelete The round selected for deletion, if any
 * @param selectedTab The currently selected tab in the game editor
 * @param showInvitationDialog Whether to show the game invitation dialog
 * @param showRenameDialog Whether to show the game rename dialog
 * @param isRefreshing Whether a data refresh is in progress
 */
private class GameEditorState(val gameId: Uuid, private val dataProvider: DataProvider) {
  var game by mutableStateOf<Game?>(null)
  var editingRound by mutableStateOf<Round?>(null)
  var showDeleteDialog by mutableStateOf(false)
  var roundToDelete by mutableStateOf<Round?>(null)
  var selectedTab by mutableStateOf(GameScreenTab.EDIT_GAME)
  var showInvitationDialog by mutableStateOf(false)
  var showRenameDialog by mutableStateOf(false)
  var isRefreshing by mutableStateOf(false)

  suspend fun loadGame() {
    game = dataProvider.getGame(gameId)
  }

  fun triggerRefresh() {
    isRefreshing = true
  }

  suspend fun performRefresh() {
    dataProvider.syncData()
    loadGame()
    isRefreshing = false
  }

  suspend fun addRound(round: Round) {
    dataProvider.addRound(gameId, round)
    loadGame()
  }

  suspend fun updateRound(round: Round) {
    dataProvider.updateRound(round)
    loadGame()
  }

  suspend fun deleteRound(roundId: Uuid) {
    dataProvider.deleteRound(roundId)
    loadGame()
  }

  suspend fun renameGame(newName: String) {
    dataProvider.renameGame(gameId, newName)
    loadGame()
  }
}

@Composable
private fun rememberGameEditorState(gameId: Uuid, dataProvider: DataProvider): GameEditorState {
  val state = remember(gameId) { GameEditorState(gameId, dataProvider) }
  val scope = rememberCoroutineScope()

  LaunchedEffect(gameId) { state.loadGame() }
  LaunchedEffect(state.isRefreshing) {
    if (state.isRefreshing) {
      scope.launch { state.performRefresh() }
    }
  }
  return state
}

/** Content for the [Edit Game][GameScreenTab.EDIT_GAME] tab in the Game Editor screen. */
@Composable
private fun GameEditingTabContent(state: GameEditorState) {
  val game = state.game ?: return
  val coroutineScope = rememberCoroutineScope()
  val globalScores = Scores.globalScores(game)

  Column(Modifier.fillMaxSize()) {
    GameHeader(
      currentGame = game,
      showInvitationDialog = { state.showInvitationDialog = true },
      showRenameDialog = { state.showRenameDialog = true },
    )

    Spacer(Modifier.size(16.dp))

    PlayerScoresRow(playerScores = game.players.map { it.name to (globalScores.scores[it] ?: 0) })

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      item {
        Spacer(Modifier.size(16.dp))
        RoundEditor(
          game = game,
          onValidate = { round -> coroutineScope.launch { state.addRound(round) } },
        )
      }

      item { HorizontalDivider() }

      item {
        Text(
          stringResource(Res.string.game_editor_round_history, game.rounds.size),
          style = MaterialTheme.typography.titleMedium,
        )
      }

      if (game.rounds.isEmpty()) {
        item {
          EmptyState(
            message = stringResource(Res.string.game_editor_empty_state),
            modifier = Modifier.fillParentMaxHeight(0.3f),
          )
        }
      } else {
        items(game.rounds.reversed()) { round ->
          RoundCard(
            round = round,
            game = game,
            onEdit = { state.editingRound = round },
            onDelete = {
              state.roundToDelete = round
              state.showDeleteDialog = true
            },
          )
        }
      }
    }
  }
}

@Composable
private fun StatsTabContent(game: Game) {
  GameStatsView(game = game, modifier = Modifier.fillMaxSize())
}

/** Centralized management of all dialogs in the Game Editor screen. */
@Composable
private fun GameEditorDialogs(state: GameEditorState, gameId: Uuid) {
  val game = state.game ?: return
  val scope = rememberCoroutineScope()

  if (state.showDeleteDialog && state.roundToDelete != null) {
    DeleteRoundDialog(
      onConfirm = {
        scope.launch {
          state.deleteRound(state.roundToDelete!!.id)
          state.showDeleteDialog = false
          state.roundToDelete = null
        }
      },
      onDismiss = {
        state.showDeleteDialog = false
        state.roundToDelete = null
      },
    )
  }

  if (state.showRenameDialog) {
    GameRenameDialog(
      currentName = game.name,
      onDismiss = { state.showRenameDialog = false },
      onConfirm = { newName ->
        scope.launch {
          state.renameGame(newName)
          state.showRenameDialog = false
        }
      },
    )
  }

  if (state.showInvitationDialog) {
    GameInvitationDialog(gameId = gameId, onDismiss = { state.showInvitationDialog = false })
  }

  if (state.editingRound != null) {
    RoundEditorDialog(
      game = game,
      existingRound = state.editingRound,
      onDismiss = { state.editingRound = null },
      onValidate = { updatedRound ->
        scope.launch {
          state.updateRound(updatedRound)
          state.editingRound = null
        }
      },
    )
  }
}

/** Simple loading state with a centered circular progress indicator. */
@Composable
private fun LoadingState() {
  Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

/**
 * Header section displaying the game name and action buttons for inviting players and renaming the
 * game.
 */
@Composable
private fun GameHeader(
  currentGame: Game,
  showInvitationDialog: () -> Unit,
  showRenameDialog: () -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text(
        text = currentGame.name,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.primary,
      )
      GameSourceBadge(source = currentGame.source)
    }
    if (currentGame.source == GameSource.LOCAL) {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = showInvitationDialog) {
          Icon(
            imageVector = FontAwesomeIcons.Solid.Share,
            contentDescription = stringResource(Res.string.game_editor_invite),
            modifier = Modifier.size(20.dp),
          )
        }
        TextButton(onClick = showRenameDialog) {
          Icon(
            imageVector = FontAwesomeIcons.Regular.Edit,
            contentDescription = stringResource(Res.string.history_rename_game),
            modifier = Modifier.size(20.dp),
          )
        }
      }
    }
  }
}

@Composable
private fun RoundCard(round: Round, game: Game, onEdit: () -> Unit, onDelete: () -> Unit) {
  val showActions = remember { mutableStateOf(false) }

  Box(modifier = Modifier.fillMaxWidth()) {
    CustomElevatedCard(
      modifier = Modifier.fillMaxWidth().let { if (showActions.value) it.blur(8.dp) else it },
      onLongClick = { showActions.value = true },
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = round.contract.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
          )
          Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.secondaryContainer,
          ) {
            Text(
              text =
                pluralStringResource(
                  Res.plurals.tarot_oudlers,
                  round.oudlerCount,
                  round.oudlerCount,
                ),
              style = MaterialTheme.typography.labelMedium,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            )
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          PlayerAvatar(name = round.taker.name, size = 32.dp)
          Column {
            Text(text = round.taker.name, style = MaterialTheme.typography.bodyMedium)
            Text(
              text = stringResource(Res.string.game_editor_taker_points, round.takerPoints),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          if (round.partner != null) {
            Text(
              stringResource(Res.string.general_with),
              style = MaterialTheme.typography.bodySmall,
            )
            PlayerAvatar(name = round.partner.name, size = 32.dp)
            Text(text = round.partner.name, style = MaterialTheme.typography.bodyMedium)
          }
        }

        HorizontalDivider()

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
          val roundScore = Scores.roundScores(round, game)
          game.players.forEach { player ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = player.name.split(' ').first(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              val score = roundScore.forPlayer(player)
              ScoreText(score = score)
            }
          }
        }
      }
    }
    if (showActions.value) {
      RoundCardActions(showActions, onEdit, onDelete)
    }
  }
}

@Composable
private fun BoxScope.RoundCardActions(
  showActions: MutableState<Boolean>,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
) {
  Surface(
    modifier = Modifier.matchParentSize(),
    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
  ) {}
  Box(modifier = Modifier.matchParentSize()) {
    Row(
      modifier = Modifier.fillMaxSize().padding(16.dp),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      TextButton(onClick = { showActions.value = false }, modifier = Modifier.weight(1f)) {
        Column(
          verticalArrangement = Arrangement.spacedBy(4.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Icon(
            imageVector = FontAwesomeIcons.Solid.ChevronLeft,
            contentDescription = stringResource(Res.string.cd_navigate_back),
            modifier = Modifier.size(24.dp),
          )
          Text(
            stringResource(Res.string.general_cancel),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }
      }
      TextButton(
        onClick = {
          showActions.value = false
          onEdit()
        },
        modifier = Modifier.weight(1f),
      ) {
        Column(
          verticalArrangement = Arrangement.spacedBy(4.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Icon(
            imageVector = FontAwesomeIcons.Regular.Edit,
            contentDescription = stringResource(Res.string.cd_edit),
            modifier = Modifier.size(24.dp),
          )
          Text(
            stringResource(Res.string.game_editor_edit),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
          )
        }
      }
      TextButton(
        onClick = {
          showActions.value = false
          onDelete()
        },
        modifier = Modifier.weight(1f),
      ) {
        Column(
          verticalArrangement = Arrangement.spacedBy(4.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Icon(
            imageVector = FontAwesomeIcons.Solid.Times,
            contentDescription = stringResource(Res.string.general_delete),
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(24.dp),
          )
          Text(
            stringResource(Res.string.general_delete),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.error,
          )
        }
      }
    }
  }
}

@Composable
private fun DeleteRoundDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(Res.string.game_editor_delete_confirm_title)) },
    text = { Text(stringResource(Res.string.game_editor_delete_confirm_message)) },
    confirmButton = {
      TextButton(onClick = onConfirm) { Text(stringResource(Res.string.general_delete)) }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(Res.string.general_cancel)) }
    },
  )
}

@Composable
private fun RoundEditorDialog(
  game: Game,
  existingRound: Round?,
  onDismiss: () -> Unit,
  onValidate: (Round) -> Unit,
) {
  Dialog(onDismissRequest = onDismiss) {
    RoundEditor(
      game = game,
      existingRound = existingRound,
      onValidate = { round -> onValidate(round) },
      onCancel = onDismiss,
    )
  }
}
