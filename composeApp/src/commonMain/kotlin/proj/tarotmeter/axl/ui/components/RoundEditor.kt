package proj.tarotmeter.axl.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlin.uuid.Uuid
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import proj.tarotmeter.axl.core.data.model.Game
import proj.tarotmeter.axl.core.data.model.Round
import proj.tarotmeter.axl.core.data.model.enums.Chelem
import proj.tarotmeter.axl.core.data.model.enums.Contract
import proj.tarotmeter.axl.core.data.model.enums.PetitAuBout
import proj.tarotmeter.axl.core.data.model.enums.Poignee
import proj.tarotmeter.axl.util.DateUtil
import tarotmeter.composeapp.generated.resources.*
import tarotmeter.composeapp.generated.resources.Res

/**
 * Holds all state for the round editor form.
 *
 * @param takerIndex Index of the taker in the game's player list
 * @param partnerIndex Index of the partner in the game's player list (-1 if no partner)
 * @param contract Selected contract
 * @param oudlerCount Number of oudlers
 * @param pointsText Card points as text
 * @param poignee Selected poignée bonus
 * @param petitAuBout Selected petit au bout bonus
 * @param chelem Selected chelem bonus
 */
private data class RoundEditorState(
  val takerIndex: Int,
  val partnerIndex: Int,
  val contract: Contract,
  val oudlerCount: Int,
  val pointsText: String,
  val poignee: Poignee,
  val petitAuBout: PetitAuBout,
  val chelem: Chelem,
) {
  companion object {
    /** Creates initial state from an existing round or defaults. */
    fun from(game: Game, existingRound: Round?): RoundEditorState {
      return RoundEditorState(
        takerIndex = existingRound?.let { game.players.indexOf(it.taker) } ?: 0,
        partnerIndex =
          existingRound?.partner?.let { game.players.indexOf(it) }
            ?: if (game.players.size == 5) 1 else -1,
        contract = existingRound?.contract ?: Contract.GARDE,
        oudlerCount = existingRound?.oudlerCount ?: 1,
        pointsText = existingRound?.takerPoints?.toString() ?: "41",
        poignee = existingRound?.poignee ?: Poignee.NONE,
        petitAuBout = existingRound?.petitAuBout ?: PetitAuBout.NONE,
        chelem = existingRound?.chelem ?: Chelem.NONE,
      )
    }

    /** Creates default state for a new round. */
    fun default(game: Game): RoundEditorState {
      return RoundEditorState(
        takerIndex = 0,
        partnerIndex = if (game.players.size == 5) 1 else -1,
        contract = Contract.GARDE,
        oudlerCount = 1,
        pointsText = "41",
        poignee = Poignee.NONE,
        petitAuBout = PetitAuBout.NONE,
        chelem = Chelem.NONE,
      )
    }
  }
}

/**
 * Composable for adding or editing a round to a game.
 *
 * @param game The current game
 * @param existingRound The round to edit, or null to create a new round
 * @param onValidate Callback when a round is created or updated
 */
@Composable
fun RoundEditor(
  game: Game,
  existingRound: Round? = null,
  onValidate: (Round) -> Unit,
  onCancel: (() -> Unit)? = null,
) {
  var state by
    remember(existingRound) { mutableStateOf(RoundEditorState.from(game, existingRound)) }
  var showBonusDialog by remember { mutableStateOf(false) }

  CustomElevatedCard(modifier = Modifier.fillMaxWidth()) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
      EditorTitle(existingRound)

      ResponsiveTwoColumn(
        leftContent = {
          PlayerSelectionColumn(
            game,
            state.takerIndex,
            state.partnerIndex,
            onTakerChange = { state = state.copy(takerIndex = it) },
            onPartnerChange = { state = state.copy(partnerIndex = it) },
          )
        },
        rightContent = {
          ContractAndOudlerColumn(
            state.contract,
            state.oudlerCount,
            onContractChange = { state = state.copy(contract = it) },
            onOudlerChange = { state = state.copy(oudlerCount = it) },
          )
        },
      )

      PointsInputField(
        pointsText = state.pointsText,
        onPointsChange = { state = state.copy(pointsText = it) },
      )

      BonusButton(
        state.poignee,
        state.petitAuBout,
        state.chelem,
        onClick = { showBonusDialog = true },
      )

      if (showBonusDialog) {
        BonusDialog(
          poignee = state.poignee,
          petitAuBout = state.petitAuBout,
          chelem = state.chelem,
          onPoigneeChange = { state = state.copy(poignee = it) },
          onPetitAuBoutChange = { state = state.copy(petitAuBout = it) },
          onChelemChange = { state = state.copy(chelem = it) },
          onDismiss = { showBonusDialog = false },
        )
      }

      Footer(existingRound = existingRound, onCancel = onCancel) {
        val round = createRound(game, state, existingRound)
        onValidate(round)
        if (existingRound == null) {
          state = RoundEditorState.default(game)
        }
      }
    }
  }
}

/** Displays the editor title based on whether adding or editing. */
@Composable
private fun EditorTitle(existingRound: Round?) {
  Text(
    stringResource(
      if (existingRound == null) Res.string.round_editor_add_new else Res.string.round_editor_edit
    ),
    style = MaterialTheme.typography.titleMedium,
  )
}

/** Player selection dropdowns (taker and optional partner). */
@Composable
private fun PlayerSelectionColumn(
  game: Game,
  takerIndex: Int,
  partnerIndex: Int,
  onTakerChange: (Int) -> Unit,
  onPartnerChange: (Int) -> Unit,
) {
  TarotDropdown(
    label = stringResource(Res.string.tarot_taker),
    options = game.players.map { it.name },
    selectedIndex = takerIndex,
    onSelect = onTakerChange,
  )

  if (game.players.size == 5) {
    TarotDropdown(
      label = stringResource(Res.string.tarot_partner),
      options = game.players.map { it.name },
      selectedIndex = partnerIndex,
      onSelect = onPartnerChange,
    )
  }
}

/** Contract and oudler selection dropdowns. */
@Composable
private fun ContractAndOudlerColumn(
  contract: Contract,
  oudler: Int,
  onContractChange: (Contract) -> Unit,
  onOudlerChange: (Int) -> Unit,
) {
  TarotDropdown(
    label = stringResource(Res.string.tarot_contract),
    options = Contract.entries.map { it.title },
    selectedIndex = Contract.entries.indexOf(contract),
    onSelect = { onContractChange(Contract.entries[it]) },
  )

  TarotDropdown(
    label = stringResource(Res.string.tarot_oudlers),
    options = (0..3).map { pluralStringResource(Res.plurals.tarot_oudlers, it, it) },
    selectedIndex = oudler,
    onSelect = onOudlerChange,
  )
}

/** Input field for card points with validation. */
@Composable
private fun PointsInputField(pointsText: String, onPointsChange: (String) -> Unit) {
  OutlinedTextField(
    value = pointsText,
    onValueChange = {
      if (it.isEmpty()) {
        onPointsChange("")
        return@OutlinedTextField
      }
      val number = it.toIntOrNull() ?: return@OutlinedTextField
      if (number in 0..91) {
        onPointsChange(number.toString())
      }
    },
    label = { Text(stringResource(Res.string.tarot_points)) },
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    modifier = Modifier.fillMaxWidth(),
  )
}

/** Button to configure bonuses with summary display. */
@Composable
private fun BonusButton(
  poignee: Poignee,
  petitAuBout: PetitAuBout,
  chelem: Chelem,
  onClick: () -> Unit,
) {
  val hasBonuses =
    poignee != Poignee.NONE || petitAuBout != PetitAuBout.NONE || chelem != Chelem.NONE
  val summary = computeBonusButtonSubtitle(poignee, petitAuBout, chelem)

  TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(stringResource(Res.string.tarot_bonuses_button))
      if (hasBonuses) {
        Text(
          summary,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.primary,
        )
      }
    }
  }
}

@Composable
private fun computeBonusButtonSubtitle(
  poignee: Poignee,
  petitAuBout: PetitAuBout,
  chelem: Chelem,
): String {
  val summary = buildString {
    if (poignee != Poignee.NONE) append(poignee.getDisplayName())
    if (petitAuBout != PetitAuBout.NONE) {
      if (isNotEmpty()) append(" • ")
      append(petitAuBout.getDisplayName())
    }
    if (chelem != Chelem.NONE) {
      if (isNotEmpty()) append(" • ")
      append(chelem.getDisplayName())
    }
  }
  return summary
}

@Composable
private fun Footer(onCancel: (() -> Unit)?, existingRound: Round?, onValidate: () -> Unit) {
  Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
    if (onCancel != null) {
      TextButton(onClick = { onCancel() }, modifier = Modifier.weight(1f)) {
        Text(stringResource(Res.string.general_cancel))
      }
    }

    PrimaryButton(
      text =
        stringResource(
          if (existingRound == null) Res.string.tarot_add else Res.string.general_save
        ),
      onClick = onValidate,
      modifier = Modifier.weight(1f),
      maxLines = 1,
    )
  }
}

/** Dialog for selecting bonuses. */
@Composable
private fun BonusDialog(
  poignee: Poignee,
  petitAuBout: PetitAuBout,
  chelem: Chelem,
  onPoigneeChange: (Poignee) -> Unit,
  onPetitAuBoutChange: (PetitAuBout) -> Unit,
  onChelemChange: (Chelem) -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(Res.string.tarot_bonuses)) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TarotDropdown(
          label = stringResource(Res.string.tarot_poignee),
          options = Poignee.entries.map { it.getDisplayName() },
          selectedIndex = Poignee.entries.indexOf(poignee),
          onSelect = { onPoigneeChange(Poignee.entries[it]) },
        )

        TarotDropdown(
          label = stringResource(Res.string.tarot_petit_au_bout),
          options = PetitAuBout.entries.map { it.getDisplayName() },
          selectedIndex = PetitAuBout.entries.indexOf(petitAuBout),
          onSelect = { onPetitAuBoutChange(PetitAuBout.entries[it]) },
        )

        TarotDropdown(
          label = stringResource(Res.string.tarot_chelem),
          options = Chelem.entries.map { it.getDisplayName() },
          selectedIndex = Chelem.entries.indexOf(chelem),
          onSelect = { onChelemChange(Chelem.entries[it]) },
        )
      }
    },
    confirmButton = {
      PrimaryButton(text = stringResource(Res.string.general_ok), onClick = onDismiss)
    },
  )
}

/** Creates a Round object from the provided state. */
private fun createRound(game: Game, state: RoundEditorState, existingRound: Round? = null): Round {
  val taker = game.players[state.takerIndex.coerceIn(0, game.players.lastIndex)]
  val partner =
    if (game.players.size == 5) game.players[state.partnerIndex.coerceIn(0, game.players.lastIndex)]
    else null
  val points = state.pointsText.toIntOrNull()
  require(points != null && points in 0..91) { "Points must be between 0 and 91" }
  return Round(
    taker = taker,
    partner = partner,
    contract = state.contract,
    oudlerCount = state.oudlerCount,
    takerPoints = points,
    poignee = state.poignee,
    petitAuBout = state.petitAuBout,
    chelem = state.chelem,
    index = existingRound?.index ?: game.rounds.size,
    id = existingRound?.id ?: Uuid.random(),
    updatedAt = DateUtil.now(),
  )
}
