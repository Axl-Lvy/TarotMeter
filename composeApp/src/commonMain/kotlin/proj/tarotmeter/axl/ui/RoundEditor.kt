package proj.tarotmeter.axl.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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

/**
 * Composable for adding a new round to a game.
 *
 * @param game The current game
 * @param onAdd Callback when a new round is created
 */
@Composable
fun RoundEditor(game: Game, onAdd: (Round) -> Unit) {
  var takerIndex by remember { mutableStateOf(0) }
  var partnerIndex by remember { mutableStateOf(if (game.players.size == 5) 1 else -1) }
  var contract by remember { mutableStateOf(Contract.GARDE) }
  var oudler by remember { mutableStateOf(1) }
  val pointsText = remember { mutableStateOf("41") }

  CustomElevatedCard(modifier = Modifier.fillMaxWidth()) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
      Text("Add New Round", style = MaterialTheme.typography.titleMedium)

      ResponsiveTwoColumn(
        leftContent = {
          TarotDropdown(
            label = "Taker",
            options = game.players.map { it.name },
            selectedIndex = takerIndex,
            onSelect = { takerIndex = it },
          )

          if (game.players.size == 5) {
            TarotDropdown(
              label = "Partner",
              options = game.players.map { it.name },
              selectedIndex = partnerIndex,
              onSelect = { partnerIndex = it },
            )
          }
        },
        rightContent = {
          TarotDropdown(
            label = "Contract",
            options = Contract.entries.map { it.title },
            selectedIndex = Contract.entries.indexOf(contract),
            onSelect = { contract = Contract.entries[it] },
          )

          TarotDropdown(
            label = "Oudlers",
            options = (0..3).map { "$it Oudler${if (it != 1) "s" else ""}" },
            selectedIndex = oudler,
            onSelect = { oudler = it },
          )
        },
      )

      PointsInputField(pointsText)

      PrimaryButton(
        text = "Add Round",
        onClick = {
          val round =
            createRound(game, takerIndex, partnerIndex, contract, oudler, pointsText.value)
          onAdd(round)
          // Reset form
          takerIndex = 0
          partnerIndex = if (game.players.size == 5) 1 else -1
          contract = Contract.GARDE
          oudler = 1
          pointsText.value = "41"
        },
        modifier = Modifier.fillMaxWidth(),
      )
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
    label = { Text("Card points (0-91)") },
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    modifier = Modifier.fillMaxWidth(),
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
    )
  return round
}
