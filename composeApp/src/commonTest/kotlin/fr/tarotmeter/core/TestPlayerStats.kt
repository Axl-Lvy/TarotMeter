package fr.tarotmeter.core

import fr.tarotmeter.core.data.model.Game
import fr.tarotmeter.core.data.model.Player
import fr.tarotmeter.core.data.model.Round
import fr.tarotmeter.core.data.model.calculated.PlayerStats
import fr.tarotmeter.core.data.model.enums.Chelem
import fr.tarotmeter.core.data.model.enums.Contract
import fr.tarotmeter.core.data.model.enums.PetitAuBout
import fr.tarotmeter.core.data.model.enums.Poignee
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestPlayerStats {

  private fun createPlayers(count: Int): List<Player> {
    return (1..count).map { Player("Player$it") }
  }

  private fun createRound(
    taker: Player,
    partner: Player? = null,
    contract: Contract = Contract.PETITE,
    oudlerCount: Int = 1,
    takerPoints: Int = 51,
    poignee: Poignee = Poignee.NONE,
    petitAuBout: PetitAuBout = PetitAuBout.NONE,
    chelem: Chelem = Chelem.NONE,
    index: Int = 0,
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
      index = index,
    )
  }

  @Test
  fun testEmptyGameReturnsEmptyStats() {
    val players = createPlayers(3)
    val game = Game(players, name = "Empty Game")

    val stats = PlayerStats.from(game)

    assertTrue(stats.isEmpty())
  }

  @Test
  fun testSingleRoundBasicStats() {
    val players = createPlayers(3)
    val game = Game(players, name = "Single Round")

    game.addRound(createRound(players[0], oudlerCount = 1, takerPoints = 51, index = 0))

    val stats = PlayerStats.from(game)

    assertEquals(3, stats.size)

    val takerStats = stats.find { it.player == players[0] }!!
    assertEquals(1, takerStats.totalRounds)
    assertEquals(50, takerStats.totalScore)
    assertEquals(50f, takerStats.averageScore)
    assertEquals(50, takerStats.bestScore)
    assertEquals(50, takerStats.worstScore)
    assertEquals(1, takerStats.wins)
    assertEquals(100f, takerStats.winRate)
    assertEquals(1, takerStats.takerCount)
    assertEquals(0, takerStats.partnerCount)
    assertEquals(0, takerStats.defenderCount)

    val defenderStats = stats.find { it.player == players[1] }!!
    assertEquals(1, defenderStats.totalRounds)
    assertEquals(-25, defenderStats.totalScore)
    assertEquals(-25f, defenderStats.averageScore)
    assertEquals(-25, defenderStats.bestScore)
    assertEquals(-25, defenderStats.worstScore)
    assertEquals(0, defenderStats.wins)
    assertEquals(0f, defenderStats.winRate)
    assertEquals(0, defenderStats.takerCount)
    assertEquals(0, defenderStats.partnerCount)
    assertEquals(1, defenderStats.defenderCount)
  }

  @Test
  fun testMultipleRoundsWithDifferentTakers() {
    val players = createPlayers(3)
    val game = Game(players, name = "Multiple Rounds")

    game.addRound(createRound(players[0], oudlerCount = 1, takerPoints = 60, index = 0))
    game.addRound(createRound(players[1], oudlerCount = 1, takerPoints = 45, index = 1))
    game.addRound(createRound(players[2], oudlerCount = 2, takerPoints = 41, index = 2))

    val stats = PlayerStats.from(game)

    val player0Stats = stats.find { it.player == players[0] }!!
    assertEquals(3, player0Stats.totalRounds)
    assertEquals(74, player0Stats.totalScore) // 68 - 32 + 38
    assertEquals(1, player0Stats.takerCount)
    assertEquals(2, player0Stats.defenderCount)
    assertEquals(2, player0Stats.wins)

    val player1Stats = stats.find { it.player == players[1] }!!
    assertEquals(3, player1Stats.totalRounds)
    assertEquals(-121, player1Stats.totalScore)
    assertEquals(1, player1Stats.takerCount)
    assertEquals(0, player1Stats.wins)

    val player2Stats = stats.find { it.player == players[2] }!!
    assertEquals(3, player2Stats.totalRounds)
    assertEquals(47, player2Stats.totalScore)
    assertEquals(1, player2Stats.takerCount)
    assertEquals(2, player2Stats.wins)
  }

  @Test
  fun testAverageScoreCalculation() {
    val players = createPlayers(3)
    val game = Game(players, name = "Average Test")

    game.addRound(createRound(players[0], oudlerCount = 1, takerPoints = 60, index = 0))
    game.addRound(createRound(players[1], oudlerCount = 1, takerPoints = 51, index = 1))
    game.addRound(createRound(players[2], oudlerCount = 1, takerPoints = 51, index = 2))

    val stats = PlayerStats.from(game)

    val player0Stats = stats.find { it.player == players[0] }!!
    val expectedAvg = (68 + (-25) + (-25)).toFloat() / 3
    assertEquals(expectedAvg, player0Stats.averageScore, 0.01f)
  }

  @Test
  fun testBestAndWorstScores() {
    val players = createPlayers(3)
    val game = Game(players, name = "Best/Worst Test")

    game.addRound(createRound(players[0], oudlerCount = 1, takerPoints = 70, index = 0))
    game.addRound(createRound(players[1], oudlerCount = 1, takerPoints = 51, index = 1))
    game.addRound(createRound(players[2], oudlerCount = 1, takerPoints = 40, index = 2))
    game.addRound(createRound(players[0], oudlerCount = 1, takerPoints = 45, index = 3))

    val stats = PlayerStats.from(game)

    val player0Stats = stats.find { it.player == players[0] }!!
    assertEquals(88, player0Stats.bestScore)
    assertEquals(-62, player0Stats.worstScore)
  }

  @Test
  fun testWinRateCalculation() {
    val players = createPlayers(3)
    val game = Game(players, name = "Win Rate Test")

    game.addRound(createRound(players[0], oudlerCount = 1, takerPoints = 60, index = 0))
    game.addRound(createRound(players[1], oudlerCount = 1, takerPoints = 51, index = 1))
    game.addRound(createRound(players[2], oudlerCount = 1, takerPoints = 40, index = 2))
    game.addRound(createRound(players[0], oudlerCount = 1, takerPoints = 55, index = 3))

    val stats = PlayerStats.from(game)

    val player0Stats = stats.find { it.player == players[0] }!!
    assertEquals(3, player0Stats.wins)
    assertEquals(75f, player0Stats.winRate)

    val player1Stats = stats.find { it.player == players[1] }!!
    assertEquals(2, player1Stats.wins)
    assertEquals(50f, player1Stats.winRate)
  }

  @Test
  fun testCumulativeTimeline() {
    val players = createPlayers(3)
    val game = Game(players, name = "Timeline Test")

    game.addRound(createRound(players[0], oudlerCount = 1, takerPoints = 51, index = 0))
    game.addRound(createRound(players[1], oudlerCount = 1, takerPoints = 51, index = 1))
    game.addRound(createRound(players[2], oudlerCount = 1, takerPoints = 51, index = 2))

    val stats = PlayerStats.from(game)

    val player0Stats = stats.find { it.player == players[0] }!!
    assertEquals(3, player0Stats.cumulativeTimeline.size)
    assertEquals(0, player0Stats.cumulativeTimeline[0].roundIndex)
    assertEquals(50, player0Stats.cumulativeTimeline[0].value)
    assertEquals(1, player0Stats.cumulativeTimeline[1].roundIndex)
    assertEquals(25, player0Stats.cumulativeTimeline[1].value)
    assertEquals(2, player0Stats.cumulativeTimeline[2].roundIndex)
    assertEquals(0, player0Stats.cumulativeTimeline[2].value)
  }

  @Test
  fun testRoleCountsIn3PlayerGame() {
    val players = createPlayers(3)
    val game = Game(players, name = "3-Player Roles")

    game.addRound(createRound(players[0], oudlerCount = 1, takerPoints = 51, index = 0))
    game.addRound(createRound(players[1], oudlerCount = 1, takerPoints = 51, index = 1))
    game.addRound(createRound(players[0], oudlerCount = 1, takerPoints = 51, index = 2))

    val stats = PlayerStats.from(game)

    val player0Stats = stats.find { it.player == players[0] }!!
    assertEquals(2, player0Stats.takerCount)
    assertEquals(0, player0Stats.partnerCount)
    assertEquals(1, player0Stats.defenderCount)

    val player1Stats = stats.find { it.player == players[1] }!!
    assertEquals(1, player1Stats.takerCount)
    assertEquals(0, player1Stats.partnerCount)
    assertEquals(2, player1Stats.defenderCount)
  }

  @Test
  fun testRoleCountsIn5PlayerGameWithPartner() {
    val players = createPlayers(5)
    val game = Game(players, name = "5-Player Roles")

    game.addRound(
      createRound(
        taker = players[0],
        partner = players[1],
        oudlerCount = 1,
        takerPoints = 51,
        index = 0,
      )
    )
    game.addRound(
      createRound(
        taker = players[2],
        partner = players[3],
        oudlerCount = 1,
        takerPoints = 51,
        index = 1,
      )
    )
    game.addRound(
      createRound(
        taker = players[0],
        partner = players[0],
        oudlerCount = 1,
        takerPoints = 51,
        index = 2,
      )
    )

    val stats = PlayerStats.from(game)

    val player0Stats = stats.find { it.player == players[0] }!!
    assertEquals(2, player0Stats.takerCount)
    assertEquals(1, player0Stats.partnerCount)
    assertEquals(1, player0Stats.defenderCount)

    val player1Stats = stats.find { it.player == players[1] }!!
    assertEquals(0, player1Stats.takerCount)
    assertEquals(1, player1Stats.partnerCount)
    assertEquals(2, player1Stats.defenderCount)

    val player4Stats = stats.find { it.player == players[4] }!!
    assertEquals(0, player4Stats.takerCount)
    assertEquals(0, player4Stats.partnerCount)
    assertEquals(3, player4Stats.defenderCount)
  }

  @Test
  fun testTakerWinRate() {
    val players = createPlayers(3)
    val game = Game(players, name = "Taker Win Rate")

    game.addRound(createRound(players[0], oudlerCount = 1, takerPoints = 60, index = 0))
    game.addRound(createRound(players[0], oudlerCount = 1, takerPoints = 45, index = 1))
    game.addRound(createRound(players[0], oudlerCount = 1, takerPoints = 55, index = 2))
    game.addRound(createRound(players[0], oudlerCount = 1, takerPoints = 40, index = 3))

    val stats = PlayerStats.from(game)

    val player0Stats = stats.find { it.player == players[0] }!!
    assertEquals(4, player0Stats.takerCount)
    assertEquals(2, player0Stats.wins)
    assertEquals(50f, player0Stats.takerWinRate)
  }

  @Test
  fun testPartnerWinRate() {
    val players = createPlayers(5)
    val game = Game(players, name = "Partner Win Rate")

    game.addRound(
      createRound(
        taker = players[0],
        partner = players[1],
        oudlerCount = 1,
        takerPoints = 60,
        index = 0,
      )
    )
    game.addRound(
      createRound(
        taker = players[2],
        partner = players[1],
        oudlerCount = 1,
        takerPoints = 45,
        index = 1,
      )
    )
    game.addRound(
      createRound(
        taker = players[3],
        partner = players[1],
        oudlerCount = 1,
        takerPoints = 55,
        index = 2,
      )
    )

    val stats = PlayerStats.from(game)

    val player1Stats = stats.find { it.player == players[1] }!!
    assertEquals(3, player1Stats.partnerCount)
    assertEquals(2, player1Stats.wins)
    assertEquals((2f / 3f) * 100f, player1Stats.partnerWinRate, 0.01f)
  }

  @Test
  fun testDefenderWinRate() {
    val players = createPlayers(3)
    val game = Game(players, name = "Defender Win Rate")

    game.addRound(createRound(players[0], oudlerCount = 1, takerPoints = 60, index = 0))
    game.addRound(createRound(players[0], oudlerCount = 1, takerPoints = 45, index = 1))
    game.addRound(createRound(players[0], oudlerCount = 1, takerPoints = 40, index = 2))

    val stats = PlayerStats.from(game)

    val player1Stats = stats.find { it.player == players[1] }!!
    assertEquals(3, player1Stats.defenderCount)
    assertEquals(2, player1Stats.wins)
    assertEquals((2f / 3f) * 100f, player1Stats.defenderWinRate, 0.01f)
  }

  @Test
  fun testZeroWinRateWhenNoRoundsInRole() {
    val players = createPlayers(5)
    val game = Game(players, name = "Zero Win Rate")

    game.addRound(
      createRound(
        taker = players[0],
        partner = players[1],
        oudlerCount = 1,
        takerPoints = 51,
        index = 0,
      )
    )

    val stats = PlayerStats.from(game)

    val player2Stats = stats.find { it.player == players[2] }!!
    assertEquals(0, player2Stats.takerCount)
    assertEquals(0, player2Stats.partnerCount)
    assertEquals(1, player2Stats.defenderCount)
    assertEquals(0f, player2Stats.takerWinRate)
    assertEquals(0f, player2Stats.partnerWinRate)
  }

  @Test
  fun testStatsSortedByTotalScoreDescending() {
    val players = createPlayers(3)
    val game = Game(players, name = "Sorting Test")

    game.addRound(createRound(players[0], oudlerCount = 1, takerPoints = 60, index = 0))
    game.addRound(createRound(players[1], oudlerCount = 1, takerPoints = 70, index = 1))
    game.addRound(createRound(players[2], oudlerCount = 1, takerPoints = 51, index = 2))

    val stats = PlayerStats.from(game)

    assertEquals(players[1], stats[0].player)
    assertEquals(players[0], stats[1].player)
    assertEquals(players[2], stats[2].player)
    assertTrue(stats[0].totalScore >= stats[1].totalScore)
    assertTrue(stats[1].totalScore >= stats[2].totalScore)
  }

  @Test
  fun testComplexGameWithMultipleContracts() {
    val players = createPlayers(4)
    val game = Game(players, name = "Complex Contracts")

    game.addRound(
      createRound(
        players[0],
        contract = Contract.PETITE,
        oudlerCount = 1,
        takerPoints = 51,
        index = 0,
      )
    )
    game.addRound(
      createRound(
        players[1],
        contract = Contract.GARDE,
        oudlerCount = 2,
        takerPoints = 50,
        index = 1,
      )
    )
    game.addRound(
      createRound(
        players[2],
        contract = Contract.GARDE_SANS,
        oudlerCount = 3,
        takerPoints = 36,
        index = 2,
      )
    )
    game.addRound(
      createRound(
        players[3],
        contract = Contract.GARDE_CONTRE,
        oudlerCount = 1,
        takerPoints = 60,
        index = 3,
      )
    )

    val stats = PlayerStats.from(game)

    assertEquals(4, stats.size)
    stats.forEach { playerStats ->
      assertEquals(4, playerStats.totalRounds)
      assertEquals(1, playerStats.takerCount)
      assertEquals(3, playerStats.defenderCount)
    }
  }

  @Test
  fun testGameWithPoignees() {
    val players = createPlayers(3)
    val game = Game(players, name = "Poignees Test")

    game.addRound(
      createRound(
        players[0],
        oudlerCount = 1,
        takerPoints = 51,
        poignee = Poignee.SIMPLE,
        index = 0,
      )
    )
    game.addRound(
      createRound(
        players[1],
        oudlerCount = 1,
        takerPoints = 51,
        poignee = Poignee.DOUBLE,
        index = 1,
      )
    )
    game.addRound(
      createRound(
        players[2],
        oudlerCount = 1,
        takerPoints = 51,
        poignee = Poignee.TRIPLE,
        index = 2,
      )
    )

    val stats = PlayerStats.from(game)

    assertEquals(3, stats.size)
    stats.forEach { assertEquals(1, it.takerCount) }
  }

  @Test
  fun testGameWithPetitAuBout() {
    val players = createPlayers(3)
    val game = Game(players, name = "Petit Au Bout Test")

    game.addRound(
      createRound(
        players[0],
        oudlerCount = 1,
        takerPoints = 51,
        petitAuBout = PetitAuBout.TAKER,
        index = 0,
      )
    )
    game.addRound(
      createRound(
        players[1],
        oudlerCount = 1,
        takerPoints = 51,
        petitAuBout = PetitAuBout.DEFENSE,
        index = 1,
      )
    )

    val stats = PlayerStats.from(game)

    val player0Stats = stats.find { it.player == players[0] }!!
    assertEquals(1, player0Stats.wins)
    assertEquals(100f, player0Stats.takerWinRate)
  }

  @Test
  fun testGameWithChelem() {
    val players = createPlayers(3)
    val game = Game(players, name = "Chelem Test")

    game.addRound(
      createRound(
        players[0],
        oudlerCount = 3,
        takerPoints = 91,
        chelem = Chelem.ANNOUNCED,
        index = 0,
      )
    )
    game.addRound(
      createRound(
        players[1],
        oudlerCount = 3,
        takerPoints = 91,
        chelem = Chelem.NOT_ANNOUNCED,
        index = 1,
      )
    )
    game.addRound(
      createRound(players[2], oudlerCount = 3, takerPoints = 85, chelem = Chelem.FAILED, index = 2)
    )

    val stats = PlayerStats.from(game)

    val player0Stats = stats.find { it.player == players[0] }!!
    assertEquals(2, player0Stats.wins)

    val player2Stats = stats.find { it.player == players[2] }!!
    assertEquals(0, player2Stats.wins)
    assertEquals(0f, player2Stats.takerWinRate)
  }

  @Test
  fun testLongGameWithManyRounds() {
    val players = createPlayers(4)
    val game = Game(players, name = "Long Game")

    repeat(20) { index ->
      val taker = players[index % 4]
      val points = 45 + (index % 20)
      game.addRound(createRound(taker, oudlerCount = 1, takerPoints = points, index = index))
    }

    val stats = PlayerStats.from(game)

    assertEquals(4, stats.size)
    stats.forEach { playerStats ->
      assertEquals(20, playerStats.totalRounds)
      assertEquals(5, playerStats.takerCount)
      assertEquals(15, playerStats.defenderCount)
    }
  }

  @Test
  fun testMixedRolesAcrossRounds() {
    val players = createPlayers(5)
    val game = Game(players, name = "Mixed Roles")

    game.addRound(
      createRound(
        taker = players[0],
        partner = players[1],
        oudlerCount = 1,
        takerPoints = 60,
        index = 0,
      )
    )
    game.addRound(
      createRound(
        taker = players[1],
        partner = players[0],
        oudlerCount = 1,
        takerPoints = 55,
        index = 1,
      )
    )
    game.addRound(
      createRound(
        taker = players[0],
        partner = players[0],
        oudlerCount = 1,
        takerPoints = 51,
        index = 2,
      )
    )
    game.addRound(
      createRound(
        taker = players[2],
        partner = players[0],
        oudlerCount = 1,
        takerPoints = 45,
        index = 3,
      )
    )

    val stats = PlayerStats.from(game)

    val player0Stats = stats.find { it.player == players[0] }!!
    assertEquals(2, player0Stats.takerCount)
    assertEquals(3, player0Stats.partnerCount)
    assertEquals(0, player0Stats.defenderCount)
    assertTrue(player0Stats.takerWinRate > 0f)
    assertTrue(player0Stats.partnerWinRate >= 0f)
  }

  @Test
  fun testAllPlayersLoseRounds() {
    val players = createPlayers(3)
    val game = Game(players, name = "All Lose")

    game.addRound(createRound(players[0], oudlerCount = 1, takerPoints = 40, index = 0))
    game.addRound(createRound(players[1], oudlerCount = 1, takerPoints = 35, index = 1))
    game.addRound(createRound(players[2], oudlerCount = 1, takerPoints = 30, index = 2))

    val stats = PlayerStats.from(game)

    stats.forEach { playerStats -> assertTrue(playerStats.totalScore <= 0 || playerStats.wins > 0) }
  }

  @Test
  fun testOnePlayerDominatesGame() {
    val players = createPlayers(3)
    val game = Game(players, name = "Domination")

    repeat(5) { index ->
      game.addRound(createRound(players[0], oudlerCount = 1, takerPoints = 70, index = index))
    }

    val stats = PlayerStats.from(game)

    val player0Stats = stats.find { it.player == players[0] }!!
    assertEquals(5, player0Stats.wins)
    assertEquals(100f, player0Stats.winRate)
    assertEquals(100f, player0Stats.takerWinRate)
    assertTrue(player0Stats.totalScore > 0)

    val player1Stats = stats.find { it.player == players[1] }!!
    assertEquals(0, player1Stats.wins)
    assertEquals(0f, player1Stats.winRate)
    assertTrue(player1Stats.totalScore < 0)
  }

  @Test
  fun testNegativeBestScoreWhenAllRoundsLost() {
    val players = createPlayers(3)
    val game = Game(players, name = "All Negative")

    game.addRound(createRound(players[0], oudlerCount = 1, takerPoints = 51, index = 0))
    game.addRound(createRound(players[0], oudlerCount = 1, takerPoints = 51, index = 1))
    game.addRound(createRound(players[0], oudlerCount = 1, takerPoints = 51, index = 2))

    val stats = PlayerStats.from(game)

    val player1Stats = stats.find { it.player == players[1] }!!
    assertTrue(player1Stats.bestScore <= 0)
    assertTrue(player1Stats.worstScore <= 0)
  }

  @Test
  fun testRoundIndexesRespectedInTimeline() {
    val players = createPlayers(3)
    val game = Game(players, name = "Timeline Order")

    game.addRound(createRound(players[0], oudlerCount = 1, takerPoints = 51, index = 0))
    game.addRound(createRound(players[1], oudlerCount = 1, takerPoints = 51, index = 1))
    game.addRound(createRound(players[2], oudlerCount = 1, takerPoints = 51, index = 2))

    val stats = PlayerStats.from(game)

    stats.forEach { playerStats ->
      val timeline = playerStats.cumulativeTimeline
      assertEquals(3, timeline.size)
      assertEquals(0, timeline[0].roundIndex)
      assertEquals(1, timeline[1].roundIndex)
      assertEquals(2, timeline[2].roundIndex)
    }
  }

  @Test
  fun testTakerAsPartnerInSolo() {
    val players = createPlayers(5)
    val game = Game(players, name = "Solo as Partner")

    game.addRound(
      createRound(
        taker = players[0],
        partner = players[0],
        oudlerCount = 1,
        takerPoints = 60,
        index = 0,
      )
    )
    game.addRound(
      createRound(
        taker = players[0],
        partner = players[0],
        oudlerCount = 1,
        takerPoints = 55,
        index = 1,
      )
    )

    val stats = PlayerStats.from(game)

    val player0Stats = stats.find { it.player == players[0] }!!
    assertEquals(2, player0Stats.takerCount)
    assertEquals(2, player0Stats.partnerCount)
    assertEquals(0, player0Stats.defenderCount)
    assertEquals(2, player0Stats.wins)
  }

  @Test
  fun testRoundsWith4Players() {
    val players = createPlayers(4)
    val game = Game(players, name = "4 Players")

    game.addRound(createRound(players[0], oudlerCount = 1, takerPoints = 51, index = 0))
    game.addRound(createRound(players[1], oudlerCount = 1, takerPoints = 51, index = 1))

    val stats = PlayerStats.from(game)

    assertEquals(4, stats.size)
    stats.forEach { assertEquals(2, it.totalRounds) }

    val player0Stats = stats.find { it.player == players[0] }!!
    assertEquals(1, player0Stats.takerCount)
    assertEquals(1, player0Stats.defenderCount)
  }

  @Test
  fun testScoreFluctuations() {
    val players = createPlayers(3)
    val game = Game(players, name = "Fluctuations")

    game.addRound(createRound(players[0], oudlerCount = 1, takerPoints = 70, index = 0))
    game.addRound(createRound(players[0], oudlerCount = 1, takerPoints = 30, index = 1))
    game.addRound(createRound(players[0], oudlerCount = 1, takerPoints = 65, index = 2))
    game.addRound(createRound(players[0], oudlerCount = 1, takerPoints = 35, index = 3))

    val stats = PlayerStats.from(game)

    val player0Stats = stats.find { it.player == players[0] }!!
    assertTrue(player0Stats.bestScore > player0Stats.worstScore)
    assertTrue(player0Stats.worstScore < 0)
  }

  @Test
  fun testAllRoleWinRatesIn5PlayerGame() {
    val players = createPlayers(5)
    val game = Game(players, name = "All Roles")

    game.addRound(
      createRound(
        taker = players[0],
        partner = players[1],
        oudlerCount = 1,
        takerPoints = 60,
        index = 0,
      )
    )
    game.addRound(
      createRound(
        taker = players[0],
        partner = players[1],
        oudlerCount = 1,
        takerPoints = 45,
        index = 1,
      )
    )

    val stats = PlayerStats.from(game)

    val player0Stats = stats.find { it.player == players[0] }!!
    assertEquals(50f, player0Stats.takerWinRate)

    val player1Stats = stats.find { it.player == players[1] }!!
    assertEquals(50f, player1Stats.partnerWinRate)

    val player2Stats = stats.find { it.player == players[2] }!!
    assertEquals(50f, player2Stats.defenderWinRate)
  }

  @Test
  fun testCumulativeScoreProgression() {
    val players = createPlayers(3)
    val game = Game(players, name = "Progression")

    game.addRound(createRound(players[0], oudlerCount = 1, takerPoints = 51, index = 0))
    game.addRound(createRound(players[1], oudlerCount = 1, takerPoints = 51, index = 1))
    game.addRound(createRound(players[2], oudlerCount = 1, takerPoints = 51, index = 2))

    val stats = PlayerStats.from(game)

    val player0Stats = stats.find { it.player == players[0] }!!
    val timeline = player0Stats.cumulativeTimeline

    var previousValue = 0
    timeline.forEach { point ->
      assertTrue(point.value >= previousValue - 200)
      previousValue = point.value
    }
  }
}
