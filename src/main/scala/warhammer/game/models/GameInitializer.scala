package warhammer.game.models

import UnitState.ALIVE_STATE

object GameInitializer {
  def createGameUnit(character: GameCharacter, location: Coordinates): GameUnit = {
    GameUnit(
      character = character,
      coordinates = location,
      state = ALIVE_STATE
    )
  }

  def initializeGameUnits(): (List[GameUnit], List[GameUnit]) = {
    val player1Unit = Characters.SpaceMarine
    val player1UnitLocation = Coordinates(3, 4)
    val unit1 = createGameUnit(player1Unit, player1UnitLocation)

    val player2Unit = Characters.Ork
    val player2UnitLocation = Coordinates(6, 8)
    val unit2 = createGameUnit(player2Unit, player2UnitLocation)

    val player2BigShootaUnit = Characters.OrkWithBigShoota
    val player2BigShootaUnitLocation = Coordinates(3, 5)
    val unit3 = createGameUnit(player2BigShootaUnit, player2BigShootaUnitLocation)

    val player2BigChoppaUnit = Characters.OrkWithBigChoppa
    val player2BigChoppaUnitLocation = Coordinates(10, 10)
    val unit4 = createGameUnit(player2BigChoppaUnit, player2BigChoppaUnitLocation)

    val player2SkorchaUnit = Characters.OrkWithScorcha
    val player2SkorchaUnitLocation = Coordinates(1, 4)
    val unit5 = createGameUnit(player2SkorchaUnit, player2SkorchaUnitLocation)

    val player1Units = List(unit1)
    val player2Units = List(unit2, unit3, unit4, unit5)

    (player1Units, player2Units)
  }
}

