package warhammer.game

import warhammer.game.models._
import org.scalatest.funspec.AnyFunSpec
import warhammer.Main.cache
import warhammer.game.models.Characters.Ork
import warhammer.game.models.UnitState.{ALIVE_STATE, DEAD_STATE}

class CloseCombatSpec extends AnyFunSpec {

  describe("checkCloseCombatAttack") {
    it("should return an empty list of targets") {
      val mapConfig = Maps.RockyDivide

      val passivePlayers = List(
        Characters.Ork,
        Characters.OrkWithBigShoota,
        Characters.OrkWithScorcha,
        Characters.OrkWithBigChoppa
      )
      val activePlayer = Characters.SpaceMarine
      val manager = new CloseCombatManager

      val result = manager.checkCloseCombatAttack(mapConfig, activePlayer, passivePlayers)

      assert(result === List())
    }
    it("should return list of targets") {
      val mapConfig = Maps.RockyDivide
      val activePlayers = List(
        Characters.SpaceMarine,
      )
      val passivePlayers = List(
        Characters.Ork,
        Characters.OrkWithBigShoota,
        Characters.OrkWithScorcha,
        Characters.OrkWithBigChoppa
      )
      val modifiedOrk = Ork.copy(currentPosition = Coordinates(x = 4, y = 4))

      val modifiedPassivePlayers = passivePlayers.map {
        case ork if ork == Characters.Ork => modifiedOrk
        case otherCharacter => otherCharacter
      }

      val activePlayer = Characters.SpaceMarine
      val manager = new CloseCombatManager

      val result = manager.checkCloseCombatAttack(mapConfig, activePlayer, modifiedPassivePlayers)

      assert(result === List(GameCharacter("a0d58842-7ad6-11ee-b72f-325096b39f47", 5, 25, 55, 4, "O", "OrkWithSluggaAndChoppa", "Ork unleashed his slugga and blasted the ", "Ork dakka dakka dakka dakka missed the ", "Ork smashed the Space Marine with his choppa, Waaaaaggh!!! ", "The Space Marine dodged the wild of swing of the choppa", Coordinates(4, 4), false, false, false, false, "alive")))
    }
  }

  describe("performCloseCombatAttackHttp") {
    it("should return board after close combat attack") {
      val targetCoordinates = Coordinates(x = 4, y = 4) // Create Coordinates instance
      val map = Maps.RockyDivide
      val attackingSpaceMarine = Characters.SpaceMarine.copy(weaponSkill = 100)
      val activePlayer = attackingSpaceMarine
      val targetOrk = Ork.copy(currentPosition = targetCoordinates)
      val potentialTargets = List(targetOrk)

      val board = Board(
        boardId = "123",
        player1 = List(attackingSpaceMarine),
        player2 = List(
          targetOrk,
          Characters.OrkWithBigShoota,
          Characters.OrkWithScorcha,
          Characters.OrkWithBigChoppa
        ),
        map = map,
        isMovePhase = true,
        isShootingPhase = false,
        isCloseCombatPhase = false,
        isTopOfTurn = true,
        isPlayer1Turn = true,
        turnNumber = 0
      )


      val manager = new CloseCombatManager

      val result = manager.performCloseCombatAttackHttp(map, activePlayer, targetCoordinates, potentialTargets, board)

      assert(result._1.boardId === "123")
      assert(result._1.player2.find(_.currentPosition == targetCoordinates).map(_.state).getOrElse(ALIVE_STATE) === DEAD_STATE)


    }
  }
}

