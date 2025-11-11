package proj.tarotmeter.axl.ui.pages

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
import kotlin.uuid.Uuid
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import proj.tarotmeter.axl.core.data.model.Game
import proj.tarotmeter.axl.core.data.model.GameSource
import proj.tarotmeter.axl.core.data.model.Round
import proj.tarotmeter.axl.core.data.model.Scores
import proj.tarotmeter.axl.core.provider.GamesProvider
import proj.tarotmeter.axl.ui.components.CustomElevatedCard
import proj.tarotmeter.axl.ui.components.EmptyState
import proj.tarotmeter.axl.ui.components.GameInvitationDialog
import proj.tarotmeter.axl.ui.components.GameRenameDialog
import proj.tarotmeter.axl.ui.components.GameSourceBadge
import proj.tarotmeter.axl.ui.components.PlayerAvatar
import proj.tarotmeter.axl.ui.components.PlayerScoresRow
import proj.tarotmeter.axl.ui.components.RoundEditor
import proj.tarotmeter.axl.ui.components.ScoreText
import tarotmeter.composeapp.generated.resources.*
import tarotmeter.composeapp.generated.resources.Res

/**
 * Screen for editing a specific game. Displays game scores, allows adding rounds, and shows round
 * history.
 *
 * @param gameId The ID of the game to edit
 */
@Composable
fun GameEditorScreen(gameId: Uuid, gamesProvider: GamesProvider = koinInject()) {
  var game by remember { mutableStateOf<Game?>(null) }
  var editingRound by remember { mutableStateOf<Round?>(null) }
  var showDeleteDialog by remember { mutableStateOf(false) }
  var roundToDelete by remember { mutableStateOf<Round?>(null) }
  var showInvitationDialog by remember { mutableStateOf(false) }
  var showRenameDialog by remember { mutableStateOf(false) }
  val coroutineScope = rememberCoroutineScope()

  LaunchedEffect(gameId) { game = gamesProvider.getGame(gameId) }

  val currentGame = game
  if (currentGame == null) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    return
  }

  val globalScores = Scores.globalScores(currentGame)

  Column(Modifier.fillMaxSize()) {
    Spacer(modifier = Modifier.size(16.dp))

    // Game name header with rename and invite buttons
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
          TextButton(onClick = { showInvitationDialog = true }) {
            Icon(
              imageVector = FontAwesomeIcons.Solid.Share,
              contentDescription = stringResource(Res.string.game_editor_invite),
              modifier = Modifier.size(20.dp),
            )
          }
          TextButton(onClick = { showRenameDialog = true }) {
            Icon(
              imageVector = FontAwesomeIcons.Regular.Edit,
              contentDescription = stringResource(Res.string.history_rename_game),
              modifier = Modifier.size(20.dp),
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.size(8.dp))
    // Fixed scores at the top
    PlayerScoresRow(
      playerScores = currentGame.players.map { it.name to (globalScores.scores[it] ?: 0) }
    )

    // Scrollable list with header content
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      item {
        Spacer(modifier = Modifier.size(16.dp))
        // Add/Edit round section
        RoundEditor(
          game = currentGame,
          onValidate = { round ->
            coroutineScope.launch {
              gamesProvider.addRound(currentGame.id, round)
              game = gamesProvider.getGame(gameId)
            }
          },
        )
      }

      item { HorizontalDivider() }

      item {
        Text(
          stringResource(Res.string.game_editor_round_history, currentGame.rounds.size),
          style = MaterialTheme.typography.titleMedium,
        )
      }

      if (currentGame.rounds.isEmpty()) {
        item {
          EmptyState(
            message = stringResource(Res.string.game_editor_empty_state),
            modifier = Modifier.fillParentMaxHeight(0.3f),
          )
        }
      } else {
        items(currentGame.rounds.reversed()) { round ->
          RoundCard(
            round = round,
            game = currentGame,
            onEdit = { editingRound = round },
            onDelete = {
              roundToDelete = round
              showDeleteDialog = true
            },
          )
        }
      }
    }
  }

  // Delete confirmation dialog
  if (showDeleteDialog && roundToDelete != null) {
    DeleteRoundDialog(
      onConfirm = {
        coroutineScope.launch {
          gamesProvider.deleteRound(roundToDelete!!.id)
          game = gamesProvider.getGame(gameId)
          showDeleteDialog = false
          roundToDelete = null
        }
      },
      onDismiss = {
        showDeleteDialog = false
        roundToDelete = null
      },
    )
  }

  // Rename game dialog
  if (showRenameDialog) {
    GameRenameDialog(
      currentName = currentGame.name,
      onDismiss = { showRenameDialog = false },
      onConfirm = { newName ->
        coroutineScope.launch {
          gamesProvider.renameGame(gameId, newName)
          game = gamesProvider.getGame(gameId)
          showRenameDialog = false
        }
      },
    )
  }

  // Invitation dialog
  if (showInvitationDialog) {
    GameInvitationDialog(gameId = gameId, onDismiss = { showInvitationDialog = false })
  }

  if (editingRound != null) {
    RoundEditorDialog(
      game = currentGame,
      existingRound = editingRound,
      onDismiss = { editingRound = null },
      onValidate = { updatedRound ->
        coroutineScope.launch {
          gamesProvider.updateRound(updatedRound)
          game = gamesProvider.getGame(gameId)
          editingRound = null
        }
      },
    )
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
        // Round header
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

        // Taker and partner info
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
              stringResource(Res.string.game_editor_with),
              style = MaterialTheme.typography.bodySmall,
            )
            PlayerAvatar(name = round.partner.name, size = 32.dp)
            Text(text = round.partner.name, style = MaterialTheme.typography.bodyMedium)
          }
        }

        HorizontalDivider()

        // Round scores
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

    // Blurred overlay with Edit, Delete, and Cancel buttons
    if (showActions.value) {
      RoundCardActions(showActions, onEdit, onDelete)
    }
  }
}

/**
 * Overlay with round action buttons: Edit, Delete, and Cancel
 *
 * @param showActions State controlling visibility of the actions overlay
 * @param onEdit Callback when Edit is selected
 * @param onDelete Callback when Delete is selected
 */
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

/**
 * Dialog for confirming round deletion
 *
 * @param onConfirm Callback when user confirms deletion
 * @param onDismiss Callback when user dismisses the dialog
 */
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
