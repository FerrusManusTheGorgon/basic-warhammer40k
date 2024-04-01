import org.scalatest.funspec.AnyFunSpec
import warhammer.game.models._
import io.undertow.Undertow
import org.json4s.reflect.Reflector.describe
import scalacache.Cache
import utest._
import warhammer.Main.cache
import warhammer.game.MovementManagerHttp

class JMoveSpec extends AnyFunSpec {

  describe("isValidMove") {
    it("should return true when the move is valid") {
      val mapConfig = Maps.RockyDivide
      val newCoordinates = Coordinates(6, 6) // provide valid coordinates
      val activePlayerUnit = Characters.SpaceMarine

      val passivePlayers = List(
        Characters.Ork,
        Characters.OrkWithBigShoota,
        Characters.OrkWithScorcha,
        Characters.OrkWithBigChoppa
      )

      val activePlayers = List(
        Characters.SpaceMarine,
      )

      val manager = new MovementManagerHttp
      val result = manager.isValidMove(mapConfig, newCoordinates, activePlayerUnit, passivePlayers, activePlayers)

      assert(result === true)
    }

    it("should return false when the move is invalid") {
      val mapConfig = Maps.RockyDivide
      val newCoordinates = Coordinates(5, 7) // provide valid coordinates
      val activePlayerUnit = Characters.SpaceMarine

      val passivePlayers = List(
        Characters.Ork,
        Characters.OrkWithBigShoota,
        Characters.OrkWithScorcha,
        Characters.OrkWithBigChoppa
      )

      val activePlayers = List(
        Characters.SpaceMarine,
      )

      val manager = new MovementManagerHttp
      val result = manager.isValidMove(mapConfig, newCoordinates, activePlayerUnit, passivePlayers, activePlayers)

      assert(result === false)
    }

  }
  describe("getShortestPath") {
    it("should return correct list of shortest path") {
      val mapConfig = Maps.RockyDivide
      val newCoordinates = Coordinates(10, 7) // provide valid coordinates
      val activePlayerUnit = Characters.SpaceMarine

      val passivePlayers = List(
        Characters.Ork,
        Characters.OrkWithBigShoota,
        Characters.OrkWithScorcha,
        Characters.OrkWithBigChoppa
      )


      val manager = new MovementManagerHttp
      val result = manager.getShortestPath(mapConfig, newCoordinates, activePlayerUnit, passivePlayers)

      assert(result === Some(List(Coordinates(3, 4), Coordinates(3, 5), Coordinates(3, 6), Coordinates(4, 6), Coordinates(5, 6), Coordinates(6, 6), Coordinates(7, 6), Coordinates(8, 6), Coordinates(9, 6), Coordinates(10, 6), Coordinates(10, 7))))
    }
    it("should return none") {
      val mapConfig = Maps.RockyDivide
      val newCoordinates = Coordinates(10, 10) // provide valid coordinates
      val activePlayerUnit = Characters.SpaceMarine

      val passivePlayers = List(
        Characters.Ork,
        Characters.OrkWithBigShoota,
        Characters.OrkWithScorcha,
        Characters.OrkWithBigChoppa
      )


      val manager = new MovementManagerHttp
      val result = manager.getShortestPath(mapConfig, newCoordinates, activePlayerUnit, passivePlayers)

      assert(result === None)
    }
  }
  describe("httpMove") {
    it("should the print board") {
      val board = Board(
        boardId = "123",
        player1 = List(Characters.SpaceMarine),
        player2 = List(
          Characters.Ork,
          Characters.OrkWithBigShoota,
          Characters.OrkWithScorcha,
          Characters.OrkWithBigChoppa),
        map = Maps.RockyDivide,
        isMovePhase = true,
        isShootingPhase = false,
        isCloseCombatPhase = false,
        isTopOfTurn = true,
        isPlayer1Turn = true,
        turnNumber = 0
      )
      val moveCoordinates = Coordinates(6, 6) // provide valid coordinates
      val avatar = "S"


      val manager = new MovementManagerHttp
      val result = manager.httpMove(moveCoordinates, board, avatar)

      assert(result === "(Board(123,List(GameCharacter(a617321a-7ad6-11ee-afff-325096b39f47,15,99,75,10,S,Space Marine,Space Marine opened fire with his Bolter and eliminated the ,Space Marine Bolts missed the ,Space eviscerated the xeno scum with his chain sword ,The xeno evaded the sweep of the chain sword,Coordinates(6,6),false,true,false,false,alive)),List(GameCharacter(a0d58842-7ad6-11ee-b72f-325096b39f47,5,25,55,4,O,OrkWithSluggaAndChoppa,Ork unleashed his slugga and blasted the ,Ork dakka dakka dakka dakka missed the ,Ork smashed the Space Marine  with his choppa, Waaaaaggh!!! ,The Space Marine dodged the wild of swing of the choppa,Coordinates(6,8),false,false,false,false,alive), GameCharacter(666c5b72-c1ee-11ee-bbbb-325096b39f47,6,66,25,10,9,OrkWithBigShoota,Ork unleashed his Big Shoota and blasted the ,Ork dakka dakka dakka dakka missed the ,Ork smashed the Space Marine  with his choppa, Waaaaaggh!!! ,The Space Marine dodged the wild of swing of the choppa,Coordinates(8,10),false,false,false,false,alive), GameCharacter(666c5b72-c1ee-11ee-bccb-325096b39f47,3,66,33,10,8,OrkWithScorcha,Ork unleashed his Skorcha and cooked the ,Ork Skorcha missed the ,Ork smashed the Space Marine  with his Skorcha, Waaaaaggh!!! ,The Space Marine dodged the wild of swing of the Skorcha,Coordinates(10,10),false,false,false,false,alive), GameCharacter(666c5b72-c1ee-11ee-bxxb-325096b39f47,5,1,66,20,7,OrkWithBigChoppa,Ork unleashed his Skorcha and cooked the ,Ork Skorcha missed the ,Ork smashed the Space Marine  with his Skorcha, Waaaaaggh!!! ,The Space Marine dodged the wild of swing of the Skorcha,Coordinates(1,10),false,false,false,false,alive)),MapConfig(10,10,List(Coordinates(6,5), Coordinates(7,5), Coordinates(9,5), Coordinates(10,5), Coordinates(3,3), Coordinates(2,3), Coordinates(1,3), Coordinates(2,9), Coordinates(3,9), Coordinates(6,1), Coordinates(6,2), Coordinates(5,7), Coordinates(5,8), Coordinates(9,7), Coordinates(9,8), Coordinates(9,9), Coordinates(9,10))),true,false,false,true,true,0),------+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+\n  10  |  7  |     |     |     |     |     |     |  9  |  X  |  8  |\n------+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+\n   9  |     |  X  |  X  |     |     |     |     |     |  X  |     |\n------+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+\n   8  |     |     |     |     |  X  |  O  |     |     |  X  |     |\n------+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+\n   7  |     |     |     |     |  X  |     |     |     |  X  |     |\n------+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+\n   6  |     |     |     |     |     |  S  |     |     |     |     |\n------+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+\n   5  |     |     |     |     |     |  X  |  X  |     |  X  |  X  |\n------+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+\n   4  |     |     |     |     |     |     |     |     |     |     |\n------+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+\n   3  |  X  |  X  |  X  |     |     |     |     |     |     |     |\n------+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+\n   2  |     |     |     |     |     |  X  |     |     |     |     |\n------+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+\n   1  |     |     |     |     |     |  X  |     |     |     |     |\n------+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+\n         1     2     3     4     5     6     7     8     9    10\nGameCharacter(a617321a-7ad6-11ee-afff-325096b39f47,15,99,75,10,S,Space Marine,Space Marine opened fire with his Bolter and eliminated the ,Space Marine Bolts missed the ,Space eviscerated the xeno scum with his chain sword ,The xeno evaded the sweep of the chain sword,Coordinates(6,6),false,true,false,false,alive))")
    }


  }
}

