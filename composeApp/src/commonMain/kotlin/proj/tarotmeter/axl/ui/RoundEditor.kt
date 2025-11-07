package proj.tarotmeter.axl.ui

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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import proj.tarotmeter.axl.core.data.model.Game
import proj.tarotmeter.axl.core.data.model.Round
import proj.tarotmeter.axl.core.data.model.enums.Chelem
import proj.tarotmeter.axl.core.data.model.enums.Contract
import proj.tarotmeter.axl.core.data.model.enums.PetitAuBout
import proj.tarotmeter.axl.core.data.model.enums.Poignee
import proj.tarotmeter.axl.ui.components.CustomElevatedCard
import proj.tarotmeter.axl.ui.components.PrimaryButton
import proj.tarotmeter.axl.ui.components.ResponsiveTwoColumn
import proj.tarotmeter.axl.ui.components.TarotDropdown
import tarotmeter.composeapp.generated.resources.*
import tarotmeter.composeapp.generated.resources.Res

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
  var takerIndex by
    remember(existingRound) { mutableStateOf(getInitialTakerIndex(game, existingRound)) }
  var partnerIndex by
    remember(existingRound) { mutableStateOf(getInitialPartnerIndex(game, existingRound)) }
  var contract by
    remember(existingRound) { mutableStateOf(existingRound?.contract ?: Contract.GARDE) }
  var oudler by remember(existingRound) { mutableStateOf(existingRound?.oudlerCount ?: 1) }
  val pointsText =
    remember(existingRound) { mutableStateOf(existingRound?.takerPoints?.toString() ?: "41") }
  var poignee by remember(existingRound) { mutableStateOf(existingRound?.poignee ?: Poignee.NONE) }
  var petitAuBout by
    remember(existingRound) { mutableStateOf(existingRound?.petitAuBout ?: PetitAuBout.NONE) }
  var chelem by remember(existingRound) { mutableStateOf(existingRound?.chelem ?: Chelem.NONE) }
  var showBonusDialog by remember { mutableStateOf(false) }

  CustomElevatedCard(modifier = Modifier.fillMaxWidth()) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
      EditorTitle(existingRound)

      ResponsiveTwoColumn(
        leftContent = {
          PlayerSelectionColumn(
            game,
            takerIndex,
            partnerIndex,
            onTakerChange = { takerIndex = it },
            onPartnerChange = { partnerIndex = it },
          )
        },
        rightContent = {
          ContractAndOudlerColumn(
            contract,
            oudler,
            onContractChange = { contract = it },
            onOudlerChange = { oudler = it },
          )
        },
      )

      PointsInputField(pointsText)

      BonusButton(poignee, petitAuBout, chelem, onClick = { showBonusDialog = true })

      if (showBonusDialog) {
        BonusDialog(
          poignee = poignee,
          petitAuBout = petitAuBout,
          chelem = chelem,
          onPoigneeChange = { poignee = it },
          onPetitAuBoutChange = { petitAuBout = it },
          onChelemChange = { chelem = it },
          onDismiss = { showBonusDialog = false },
        )
      }

      Footer(existingRound = existingRound, onCancel = onCancel) {
        val round =
          createRound(
            game,
            takerIndex,
            partnerIndex,
            contract,
            oudler,
            pointsText.value,
            poignee,
            petitAuBout,
            chelem,
            existingRound,
          )
        onValidate(round)
        if (existingRound == null) {
          resetForm(
            game,
            { takerIndex = it },
            { partnerIndex = it },
            { contract = it },
            { oudler = it },
            pointsText,
            { poignee = it },
            { petitAuBout = it },
            { chelem = it },
          )
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
private fun PointsInputField(pointsText: MutableState<String>) {
  OutlinedTextField(
    value = pointsText.value,
    onValueChange = {
      if (it.isEmpty()) {
        pointsText.value = ""
        return@OutlinedTextField
      }
      val number = it.toIntOrNull() ?: return@OutlinedTextField
      if (number in 0..91) {
        pointsText.value = number.toString()
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
  val summary = buildString {
    if (poignee != Poignee.NONE) append(poignee.getDisplayName())
    if (petitAuBout != PetitAuBout.NONE) {
      if (isNotEmpty()) append(" • ")
      append(getPetitAuBoutName(petitAuBout))
    }
    if (chelem != Chelem.NONE) {
      if (isNotEmpty()) append(" • ")
      append(getChelemName(chelem))
    }
  }

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
          options = PetitAuBout.entries.map { getPetitAuBoutName(it) },
          selectedIndex = PetitAuBout.entries.indexOf(petitAuBout),
          onSelect = { onPetitAuBoutChange(PetitAuBout.entries[it]) },
        )

        TarotDropdown(
          label = stringResource(Res.string.tarot_chelem),
          options = Chelem.entries.map { getChelemName(it) },
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

/** Gets the display name for a Petit au Bout value. */
@Composable
private fun getPetitAuBoutName(petitAuBout: PetitAuBout): String {
  return stringResource(
    when (petitAuBout) {
      PetitAuBout.NONE -> Res.string.tarot_petit_au_bout_none
      PetitAuBout.TAKER -> Res.string.tarot_petit_au_bout_taker
      PetitAuBout.DEFENSE -> Res.string.tarot_petit_au_bout_defense
    }
  )
}

/** Gets the display name for a Chelem value. */
@Composable
private fun getChelemName(chelem: Chelem): String {
  return stringResource(
    when (chelem) {
      Chelem.NONE -> Res.string.tarot_chelem_none
      Chelem.NOT_ANNOUNCED -> Res.string.tarot_chelem_not_announced
      Chelem.ANNOUNCED -> Res.string.tarot_chelem_announced
      Chelem.FAILED -> Res.string.tarot_chelem_failed
    }
  )
}

/** Creates a Round object from the provided parameters. */
private fun createRound(
  game: Game,
  takerIndex: Int,
  partnerIndex: Int,
  contract: Contract,
  oudler: Int,
  pointsText: String,
  poignee: Poignee,
  petitAuBout: PetitAuBout,
  chelem: Chelem,
  existingRound: Round? = null,
): Round {
  val taker = game.players[takerIndex.coerceIn(0, game.players.lastIndex)]
  val partner =
    if (game.players.size == 5) game.players[partnerIndex.coerceIn(0, game.players.lastIndex)]
    else null
  val points = pointsText.toIntOrNull()
  require(points != null && points in 0..91) { "Points must be between 0 and 91" }
  val round =
    Round(
      taker = taker,
      partner = partner,
      contract = contract,
      oudlerCount = oudler,
      takerPoints = points,
      poignee = poignee,
      petitAuBout = petitAuBout,
      chelem = chelem,
      index = existingRound?.index ?: game.rounds.size,
      id = existingRound?.id ?: kotlin.uuid.Uuid.random(),
      updatedAt = proj.tarotmeter.axl.util.DateUtil.now(),
    )
  return round
}

/** Gets the initial taker index from existing round or defaults to 0. */
private fun getInitialTakerIndex(game: Game, existingRound: Round?): Int {
  return existingRound?.let { game.players.indexOf(it.taker) } ?: 0
}

/** Gets the initial partner index from existing round or defaults based on player count. */
private fun getInitialPartnerIndex(game: Game, existingRound: Round?): Int {
  return existingRound?.partner?.let { game.players.indexOf(it) }
    ?: if (game.players.size == 5) 1 else -1
}

/** Resets the form to default values after adding a round. */
private fun resetForm(
  game: Game,
  setTakerIndex: (Int) -> Unit,
  setPartnerIndex: (Int) -> Unit,
  setContract: (Contract) -> Unit,
  setOudler: (Int) -> Unit,
  pointsText: MutableState<String>,
  setPoignee: (Poignee) -> Unit,
  setPetitAuBout: (PetitAuBout) -> Unit,
  setChelem: (Chelem) -> Unit,
) {
  setTakerIndex(0)
  setPartnerIndex(if (game.players.size == 5) 1 else -1)
  setContract(Contract.GARDE)
  setOudler(1)
  pointsText.value = "41"
  setPoignee(Poignee.NONE)
  setPetitAuBout(PetitAuBout.NONE)
  setChelem(Chelem.NONE)
}
