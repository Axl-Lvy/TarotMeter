package proj.tarotmeter.axl.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import proj.tarotmeter.axl.core.data.model.Game
import proj.tarotmeter.axl.core.data.model.Player
import proj.tarotmeter.axl.core.data.model.Round
import proj.tarotmeter.axl.core.data.model.Scores
import proj.tarotmeter.axl.core.data.model.enums.Chelem
import proj.tarotmeter.axl.core.data.model.enums.Contract
import proj.tarotmeter.axl.core.data.model.enums.PetitAuBout
import proj.tarotmeter.axl.core.data.model.enums.Poignee

class TestScoresCalculation {

  private fun createPlayers(count: Int): List<Player> {
    return (1..count).map { Player("Player$it") }
  }

  private fun createBasicRound(
    taker: Player,
    partner: Player? = null,
    contract: Contract = Contract.PETITE,
    oudlerCount: Int = 1,
    takerPoints: Int = 51,
    poignee: Poignee = Poignee.NONE,
    petitAuBout: PetitAuBout = PetitAuBout.NONE,
    chelem: Chelem = Chelem.NONE,
  ): Round {
    return Round(
      taker = taker,
      partner = partner,
      contract = contract,
      oudlerCount = oudlerCount,
      takerPoints = takerPoints,
      poignee = poignee,
      petitAuBout = petitAuBout,
      chelem = chelem,
    )
  }

  @Test
  fun testTargetPointsForOudlers() {
    // Test target points calculation based on oudlers
    val players = createPlayers(3)
    val game = Game(players)

    // 0 oudlers = 56 points needed
    val round0 = createBasicRound(players[0], oudlerCount = 0, takerPoints = 56)
    val scores0 = Scores.roundScores(round0, game)
    assertEquals(50, scores0.forPlayer(players[0])) // 25 + 0 = 25, * 2 = 50

    // 1 oudler = 51 points needed
    val round1 = createBasicRound(players[0], oudlerCount = 1, takerPoints = 51)
    val scores1 = Scores.roundScores(round1, game)
    assertEquals(50, scores1.forPlayer(players[0])) // 25 + 0 = 25, * 2 = 50

    // 2 oudlers = 41 points needed
    val round2 = createBasicRound(players[0], oudlerCount = 2, takerPoints = 41)
    val scores2 = Scores.roundScores(round2, game)
    assertEquals(50, scores2.forPlayer(players[0])) // 25 + 0 = 25, * 2 = 50

    // 3 oudlers = 36 points needed
    val round3 = createBasicRound(players[0], oudlerCount = 3, takerPoints = 36)
    val scores3 = Scores.roundScores(round3, game)
    assertEquals(50, scores3.forPlayer(players[0])) // 25 + 0 = 25, * 2 = 50
  }

  @Test
  fun testBasicScoreCalculation3Players() {
    val players = createPlayers(3)
    val game = Game(players)

    // Taker wins with exact target
    val winningRound = createBasicRound(players[0], oudlerCount = 1, takerPoints = 51)
    val winningScores = Scores.roundScores(winningRound, game)

    assertEquals(50, winningScores.forPlayer(players[0])) // Taker: 25 * 2
    assertEquals(-25, winningScores.forPlayer(players[1])) // Defender 1: -25
    assertEquals(-25, winningScores.forPlayer(players[2])) // Defender 2: -25

    // Taker loses
    val losingRound = createBasicRound(players[0], oudlerCount = 1, takerPoints = 50)
    val losingScores = Scores.roundScores(losingRound, game)

    assertEquals(-52, losingScores.forPlayer(players[0])) // Taker: -(25 + 1) * 2
    assertEquals(26, losingScores.forPlayer(players[1])) // Defender 1: 25 + 1
    assertEquals(26, losingScores.forPlayer(players[2])) // Defender 2: 25 + 1
  }

  @Test
  fun testBasicScoreCalculation4Players() {
    val players = createPlayers(4)
    val game = Game(players)

    // Taker wins with exact target
    val winningRound = createBasicRound(players[0], oudlerCount = 1, takerPoints = 51)
    val winningScores = Scores.roundScores(winningRound, game)

    assertEquals(75, winningScores.forPlayer(players[0])) // Taker: 25 * 3
    assertEquals(-25, winningScores.forPlayer(players[1])) // Defender 1: -25
    assertEquals(-25, winningScores.forPlayer(players[2])) // Defender 2: -25
    assertEquals(-25, winningScores.forPlayer(players[3])) // Defender 3: -25
  }

  @Test
  fun testBasicScoreCalculation5Players() {
    val players = createPlayers(5)
    val game = Game(players)

    // Taker with partner (different players)
    val winningRound =
      createBasicRound(taker = players[0], partner = players[1], oudlerCount = 1, takerPoints = 51)
    val winningScores = Scores.roundScores(winningRound, game)

    assertEquals(50, winningScores.forPlayer(players[0])) // Taker: 25 * 2
    assertEquals(25, winningScores.forPlayer(players[1])) // Partner: 25 * 1
    assertEquals(-25, winningScores.forPlayer(players[2])) // Defender 1: -25
    assertEquals(-25, winningScores.forPlayer(players[3])) // Defender 2: -25
    assertEquals(-25, winningScores.forPlayer(players[4])) // Defender 3: -25

    // Taker as own partner (solo)
    val soloRound =
      createBasicRound(taker = players[0], partner = players[0], oudlerCount = 1, takerPoints = 51)
    val soloScores = Scores.roundScores(soloRound, game)

    assertEquals(75, soloScores.forPlayer(players[0])) // Taker: 25 * 2 + 25 * 1
    assertEquals(-25, soloScores.forPlayer(players[1])) // Defender 1: -25
    assertEquals(-25, soloScores.forPlayer(players[2])) // Defender 2: -25
    assertEquals(-25, soloScores.forPlayer(players[3])) // Defender 3: -25
    assertEquals(-25, soloScores.forPlayer(players[4])) // Defender 4: -25
  }

  @Test
  fun testContractMultipliers() {
    val players = createPlayers(3)
    val game = Game(players)

    // Test all contract types with winning scenario
    val basePoints = 51 // 1 oudler target

    // Petite (1x)
    val petiteRound =
      createBasicRound(players[0], contract = Contract.PETITE, takerPoints = basePoints)
    val petiteScores = Scores.roundScores(petiteRound, game)
    assertEquals(50, petiteScores.forPlayer(players[0])) // 25 * 1 * 2

    // Garde (2x)
    val gardeRound =
      createBasicRound(players[0], contract = Contract.GARDE, takerPoints = basePoints)
    val gardeScores = Scores.roundScores(gardeRound, game)
    assertEquals(100, gardeScores.forPlayer(players[0])) // 25 * 2 * 2

    // Garde Sans (4x)
    val gardeSansRound =
      createBasicRound(players[0], contract = Contract.GARDE_SANS, takerPoints = basePoints)
    val gardeSansScores = Scores.roundScores(gardeSansRound, game)
    assertEquals(200, gardeSansScores.forPlayer(players[0])) // 25 * 4 * 2

    // Garde Contre (6x)
    val gardeContreRound =
      createBasicRound(players[0], contract = Contract.GARDE_CONTRE, takerPoints = basePoints)
    val gardeContreScores = Scores.roundScores(gardeContreRound, game)
    assertEquals(300, gardeContreScores.forPlayer(players[0])) // 25 * 6 * 2
  }

  @Test
  fun testPoigneeBonus() {
    val players = createPlayers(3)
    val game = Game(players)

    // Test different poignee values when taker wins
    val baseRound = createBasicRound(players[0], oudlerCount = 1, takerPoints = 51)

    // Simple poignee
    val simplePoigneeRound = baseRound.copy(poignee = Poignee.SIMPLE)
    val simpleScores = Scores.roundScores(simplePoigneeRound, game)
    assertEquals(90, simpleScores.forPlayer(players[0]))

    // Double poignee
    val doublePoigneeRound = baseRound.copy(poignee = Poignee.DOUBLE)
    val doubleScores = Scores.roundScores(doublePoigneeRound, game)
    assertEquals(110, doubleScores.forPlayer(players[0]))

    // Triple poignee
    val triplePoigneeRound = baseRound.copy(poignee = Poignee.TRIPLE)
    val tripleScores = Scores.roundScores(triplePoigneeRound, game)
    assertEquals(130, tripleScores.forPlayer(players[0]))

    // Test poignee when taker loses (should still add bonus)
    val losingPoigneeRound =
      createBasicRound(players[0], oudlerCount = 1, takerPoints = 50, poignee = Poignee.SIMPLE)
    val losingScores = Scores.roundScores(losingPoigneeRound, game)
    assertEquals(-92, losingScores.forPlayer(players[0]))
  }

  @Test
  fun testPetitAuBout() {
    val players = createPlayers(3)
    val game = Game(players)

    // Petit au bout won by taker
    val takerPetitRound =
      createBasicRound(
        players[0],
        oudlerCount = 1,
        takerPoints = 51,
        petitAuBout = PetitAuBout.TAKER,
        contract = Contract.GARDE, // 2x multiplier
      )
    val takerPetitScores = Scores.roundScores(takerPetitRound, game)
    assertEquals(140, takerPetitScores.forPlayer(players[0])) // (25 * 2 + 10 * 2) * 2

    // Petit au bout won by defense
    val defensePetitRound =
      createBasicRound(
        players[0],
        oudlerCount = 1,
        takerPoints = 51,
        petitAuBout = PetitAuBout.DEFENSE,
        contract = Contract.GARDE, // 2x multiplier
      )
    val defensePetitScores = Scores.roundScores(defensePetitRound, game)
    assertEquals(60, defensePetitScores.forPlayer(players[0])) // (25 * 2 - 10 * 2) * 2
  }

  @Test
  fun testChelem() {
    val players = createPlayers(3)
    val game = Game(players)

    // Non-announced chelem
    val nonAnnouncedRound =
      createBasicRound(players[0], oudlerCount = 3, takerPoints = 91, chelem = Chelem.NOT_ANNOUNCED)
    val nonAnnouncedScores = Scores.roundScores(nonAnnouncedRound, game)
    assertEquals(
      560,
      nonAnnouncedScores.forPlayer(players[0]),
    ) // (25 + 55) * 2 + 200 * 3 = 160 + 600 - 110 = 650

    // Announced chelem
    val announcedRound =
      createBasicRound(players[0], oudlerCount = 3, takerPoints = 91, chelem = Chelem.ANNOUNCED)
    val announcedScores = Scores.roundScores(announcedRound, game)
    assertEquals(
      960,
      announcedScores.forPlayer(players[0]),
    ) // (25 + 55) * 2 + 400 * 3 = 160 + 1200 - 310 = 1050

    // Failed chelem
    val failedRound =
      createBasicRound(players[0], oudlerCount = 3, takerPoints = 90, chelem = Chelem.FAILED)
    val failedScores = Scores.roundScores(failedRound, game)
    assertEquals(-242, failedScores.forPlayer(players[0])) // -(25 + 1) * 2 - 200 = -52 - 200 = -252
  }

  @Test
  fun testComplexScenario() {
    val players = createPlayers(4)
    val game = Game(players)

    // Complex scenario: Garde Sans + Double Poignee + Petit au Bout + high points
    val complexRound =
      createBasicRound(
        players[0],
        contract = Contract.GARDE_SANS, // 4x multiplier
        oudlerCount = 2, // target = 41
        takerPoints = 70, // +29 points
        poignee = Poignee.DOUBLE, // +30 points
        petitAuBout = PetitAuBout.TAKER, // +10 * 4 = +40 points
      )
    val complexScores = Scores.roundScores(complexRound, game)

    // Base calculation: (25 + 29) * 4 = 216
    // Poignee: +30
    // Petit au Bout: +10 * 4 = +40
    // Total: 216 + 30 + 40 = 286
    // Taker gets: 286 * 3 = 858
    assertEquals(858, complexScores.forPlayer(players[0]))
    assertEquals(-286, complexScores.forPlayer(players[1]))
    assertEquals(-286, complexScores.forPlayer(players[2]))
    assertEquals(-286, complexScores.forPlayer(players[3]))
  }

  @Test
  fun testAggregateScores() {
    val players = createPlayers(3)

    val scores1 = Scores(mapOf(players[0] to 100, players[1] to -50, players[2] to -50))
    val scores2 = Scores(mapOf(players[0] to -75, players[1] to 25, players[2] to 50))
    val scores3 = Scores(mapOf(players[0] to 50, players[1] to 0, players[2] to -50))

    val aggregated = Scores.aggregateScores(scores1, scores2, scores3)

    assertEquals(75, aggregated.forPlayer(players[0])) // 100 - 75 + 50
    assertEquals(-25, aggregated.forPlayer(players[1])) // -50 + 25 + 0
    assertEquals(-50, aggregated.forPlayer(players[2])) // -50 + 50 - 50
  }

  @Test
  fun testGlobalScores() {
    val players = createPlayers(3)
    val game = Game(players)

    val round1 = createBasicRound(players[0], oudlerCount = 1, takerPoints = 60) // Taker wins big
    val round2 = createBasicRound(players[1], oudlerCount = 1, takerPoints = 45) // Player 1 loses
    val round3 =
      createBasicRound(players[2], oudlerCount = 2, takerPoints = 41) // Player 2 exact win

    game.addRound(round1)
    game.addRound(round2)
    game.addRound(round3)

    val globalScores = Scores.globalScores(game)

    assertEquals(74, globalScores.forPlayer(players[0]))
    assertEquals(-121, globalScores.forPlayer(players[1]))
    assertEquals(47, globalScores.forPlayer(players[2]))
  }

  @Test
  fun testForPlayerWithMissingPlayer() {
    val players = createPlayers(3)
    val missingPlayer = Player("Missing")

    val scores = Scores(mapOf(players[0] to 100, players[1] to -50))

    assertEquals(100, scores.forPlayer(players[0]))
    assertEquals(-50, scores.forPlayer(players[1]))
    assertEquals(0, scores.forPlayer(players[2])) // Not in map
    assertEquals(0, scores.forPlayer(missingPlayer)) // Not in map
  }

  @Test
  fun testInvalidPlayerCount() {
    val players = createPlayers(6) // Invalid: too many players

    assertFailsWith<IllegalArgumentException> { Game(players) }
  }

  @Test
  fun testPartnerRequiredFor5Players() {
    val players = createPlayers(5)
    val game = Game(players)

    val roundWithoutPartner = createBasicRound(players[0], partner = null)

    assertFailsWith<IllegalStateException> { Scores.roundScores(roundWithoutPartner, game) }
  }
}
