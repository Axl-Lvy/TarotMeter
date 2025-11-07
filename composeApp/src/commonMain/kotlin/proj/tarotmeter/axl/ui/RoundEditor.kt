package proj.tarotmeter.axl.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
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
    remember(existingRound) {
      mutableStateOf(existingRound?.let { game.players.indexOf(it.taker) } ?: 0)
    }
  var partnerIndex by
    remember(existingRound) {
      mutableStateOf(
        existingRound?.partner?.let { game.players.indexOf(it) }
          ?: if (game.players.size == 5) 1 else -1
      )
    }
  var contract by
    remember(existingRound) { mutableStateOf(existingRound?.contract ?: Contract.GARDE) }
  var oudler by remember(existingRound) { mutableStateOf(existingRound?.oudlerCount ?: 1) }
  val pointsText =
    remember(existingRound) { mutableStateOf(existingRound?.takerPoints?.toString() ?: "41") }

  CustomElevatedCard(modifier = Modifier.fillMaxWidth()) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
      Text(
        stringResource(
          if (existingRound == null) Res.string.round_editor_add_new
          else Res.string.round_editor_edit
        ),
        style = MaterialTheme.typography.titleMedium,
      )

      ResponsiveTwoColumn(
        leftContent = {
          TarotDropdown(
            label = stringResource(Res.string.tarot_taker),
            options = game.players.map { it.name },
            selectedIndex = takerIndex,
            onSelect = { takerIndex = it },
          )

          if (game.players.size == 5) {
            TarotDropdown(
              label = stringResource(Res.string.tarot_partner),
              options = game.players.map { it.name },
              selectedIndex = partnerIndex,
              onSelect = { partnerIndex = it },
            )
          }
        },
        rightContent = {
          TarotDropdown(
            label = stringResource(Res.string.tarot_contract),
            options = Contract.entries.map { it.title },
            selectedIndex = Contract.entries.indexOf(contract),
            onSelect = { contract = Contract.entries[it] },
          )

          TarotDropdown(
            label = stringResource(Res.string.tarot_oudlers),
            options = (0..3).map { pluralStringResource(Res.plurals.tarot_oudlers, it, it) },
            selectedIndex = oudler,
            onSelect = { oudler = it },
          )
        },
      )

      PointsInputField(pointsText)

      Footer(existingRound = existingRound, onCancel = onCancel) {
        val round =
          createRound(
            game = game,
            takerIndex = takerIndex,
            partnerIndex = partnerIndex,
            contract = contract,
            oudler = oudler,
            pointsText = pointsText.value,
            existingRound = existingRound,
          )
        onValidate(round)
        // Reset form only if adding (not editing)
        if (existingRound == null) {
          takerIndex = 0
          partnerIndex = if (game.players.size == 5) 1 else -1
          contract = Contract.GARDE
          oudler = 1
          pointsText.value = "41"
        }
      }
    }
  }
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
        stringResource(if (existingRound == null) Res.string.tarot_add else Res.string.tarot_save),
      onClick = onValidate,
      modifier = Modifier.weight(1f),
    )
  }
}

/** Creates a Round object from the provided parameters. */
private fun createRound(
  game: Game,
  takerIndex: Int,
  partnerIndex: Int,
  contract: Contract,
  oudler: Int,
  pointsText: String,
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
      poignee = Poignee.NONE,
      petitAuBout = PetitAuBout.NONE,
      chelem = Chelem.NONE,
      index = existingRound?.index ?: game.rounds.size,
      id = existingRound?.id ?: kotlin.uuid.Uuid.random(),
      updatedAt = proj.tarotmeter.axl.util.DateUtil.now(),
    )
  return round
}
