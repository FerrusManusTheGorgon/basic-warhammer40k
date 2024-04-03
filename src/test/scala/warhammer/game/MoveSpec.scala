import org.scalatest.funspec.AnyFunSpec
import warhammer.game.models._
import warhammer.Main.cache
import warhammer.game.MovementManager

class MoveSpec extends AnyFunSpec {

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

      val manager = new MovementManager
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

      val manager = new MovementManager
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


      val manager = new MovementManager
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


      val manager = new MovementManager
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


      val manager = new MovementManager
      val result = manager.httpMove(moveCoordinates, board, avatar)

      assert(result._2.take(10) === "------+- -")


    }
  }
}

