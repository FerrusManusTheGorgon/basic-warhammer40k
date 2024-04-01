package warhammer.game

import warhammer.game.models._
import org.scalatest.funspec.AnyFunSpec
import warhammer.Main.cache
import warhammer.game.models.Characters.Ork

class ShootSpec extends AnyFunSpec {

  describe("checkRangeAttack") {
    it("should return an empty list of targets") {
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
      val activePlayer = Characters.SpaceMarine
      val manager = new RangeAttackMangerHttp

      val result = manager.checkRangedAttack(mapConfig, activePlayer, passivePlayers, activePlayers)

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
      val modifiedOrk = Ork.copy(currentPosition = Coordinates(x = 3, y = 6))

      val modifiedPassivePlayers = passivePlayers.map {
        case ork if ork == Characters.Ork => modifiedOrk
        case otherCharacter => otherCharacter
      }

      val activePlayer = Characters.SpaceMarine
      val manager = new RangeAttackMangerHttp

      val result = manager.checkRangedAttack(mapConfig, activePlayer, modifiedPassivePlayers, activePlayers)

      assert(result === List(GameCharacter("a0d58842-7ad6-11ee-b72f-325096b39f47", 5, 25, 55, 4, "O", "OrkWithSluggaAndChoppa", "Ork unleashed his slugga and blasted the ", "Ork dakka dakka dakka dakka missed the ", "Ork smashed the Space Marine with his choppa, Waaaaaggh!!! ", "The Space Marine dodged the wild of swing of the choppa", Coordinates(3, 6), false, false, false, false, "alive")))
    }
  }

  describe("performRangedAttackHttp") {
    it("should return board after range attack") {
      val avatar = "S"
      val targetCoordinates = Coordinates(x = 3, y = 6) // Create Coordinates instance
      val modifiedOrk = Ork.copy(currentPosition = Coordinates(x = 3, y = 6))

      val board = Board(
        boardId = "123",
        player1 = List(Characters.SpaceMarine),
        player2 = List(
          Ork.copy(currentPosition = Coordinates(x = 3, y = 6)), // Include the modified Ork in the player2 list
          Characters.OrkWithBigShoota,
          Characters.OrkWithScorcha,
          Characters.OrkWithBigChoppa
        ),
        map = Maps.RockyDivide,
        isMovePhase = true,
        isShootingPhase = false,
        isCloseCombatPhase = false,
        isTopOfTurn = true,
        isPlayer1Turn = true,
        turnNumber = 0
      )

      val manager = new RangeAttackMangerHttp

      val result = manager.performRangedAttackHttp(avatar, targetCoordinates, board)

      assert(result === "(Board(123,List(GameCharacter(a617321a-7ad6-11ee-afff-325096b39f47,15,99,75,10,S,Space Marine,Space Marine opened fire with his Bolter and eliminated the ,Space Marine Bolts missed the ,Space eviscerated the xeno scum with his chain sword ,The xeno evaded the sweep of the chain sword,Coordinates(3,4),false,false,true,false,alive)),List(GameCharacter(666c5b72-c1ee-11ee-bbbb-325096b39f47,6,66,25,10,9,OrkWithBigShoota,Ork unleashed his Big Shoota and blasted the ,Ork dakka dakka dakka dakka missed the ,Ork smashed the Space Marine  with his choppa, Waaaaaggh!!! ,The Space Marine dodged the wild of swing of the choppa,Coordinates(8,10),false,false,false,false,alive), GameCharacter(666c5b72-c1ee-11ee-bccb-325096b39f47,3,66,33,10,8,OrkWithScorcha,Ork unleashed his Skorcha and cooked the ,Ork Skorcha missed the ,Ork smashed the Space Marine  with his Skorcha, Waaaaaggh!!! ,The Space Marine dodged the wild of swing of the Skorcha,Coordinates(10,10),false,false,false,false,alive), GameCharacter(666c5b72-c1ee-11ee-bxxb-325096b39f47,5,1,66,20,7,OrkWithBigChoppa,Ork unleashed his Skorcha and cooked the ,Ork Skorcha missed the ,Ork smashed the Space Marine  with his Skorcha, Waaaaaggh!!! ,The Space Marine dodged the wild of swing of the Skorcha,Coordinates(1,10),false,false,false,false,alive), GameCharacter(a0d58842-7ad6-11ee-b72f-325096b39f47,5,25,55,4,O,OrkWithSluggaAndChoppa,Ork unleashed his slugga and blasted the ,Ork dakka dakka dakka dakka missed the ,Ork smashed the Space Marine  with his choppa, Waaaaaggh!!! ,The Space Marine dodged the wild of swing of the choppa,Coordinates(3,6),false,false,false,false,dead)),MapConfig(10,10,List(Coordinates(6,5), Coordinates(7,5), Coordinates(9,5), Coordinates(10,5), Coordinates(3,3), Coordinates(2,3), Coordinates(1,3), Coordinates(2,9), Coordinates(3,9), Coordinates(6,1), Coordinates(6,2), Coordinates(5,7), Coordinates(5,8), Coordinates(9,7), Coordinates(9,8), Coordinates(9,9), Coordinates(9,10))),true,false,false,true,true,0),Space Marine hits OrkWithSluggaAndChoppa!\n------+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+\n  10  |  7  |     |     |     |     |     |     |  9  |  X  |  8  |\n------+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+\n   9  |     |  X  |  X  |     |     |     |     |     |  X  |     |\n------+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+\n   8  |     |     |     |     |  X  |     |     |     |  X  |     |\n------+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+\n   7  |     |     |     |     |  X  |     |     |     |  X  |     |\n------+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+\n   6  |     |     |  x  |     |     |     |     |     |     |     |\n------+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+\n   5  |     |     |     |     |     |  X  |  X  |     |  X  |  X  |\n------+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+\n   4  |     |     |  S  |     |     |     |     |     |     |     |\n------+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+\n   3  |  X  |  X  |  X  |     |     |     |     |     |     |     |\n------+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+\n   2  |     |     |     |     |     |  X  |     |     |     |     |\n------+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+\n   1  |     |     |     |     |     |  X  |     |     |     |     |\n------+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+\n         1     2     3     4     5     6     7     8     9    10")

    }
  }
}

