package app

import models.{Board, Characters, Coordinates, GameCharacter, MapConfig, Maps}
import game.GameUnit
import models.UnitState.ALIVE_STATE
import jobs.{CheckVictoryConditions, CloseCombatManager2, GraveYardManager, MapUtils, MovementManager, RangeAttackManager2}
import scalacache._
import scalacache.caffeine._
import scalacache.modes.sync._
import scalacache.serialization.binary._
import cask.model.Request
import java.util.UUID


object MinimalApplication extends cask.MainRoutes {
  implicit val cache: Cache[Board] = CaffeineCache[Board]
  val player1Unit: GameCharacter = Characters.SpaceMarine
  val player1UnitLocation: Coordinates = Coordinates(3, 4)
  val unit1: GameUnit = GameUnit(
    character = player1Unit,
    coordinates = player1UnitLocation,
    state = ALIVE_STATE
  )
  val player2Unit: GameCharacter = Characters.Ork
  val player2UnitLocation: Coordinates = Coordinates(6, 8)
  val unit2: GameUnit = GameUnit(
    character = player2Unit,
    coordinates = player2UnitLocation,
    state = ALIVE_STATE
  )
  val player2BigShootaUnit: GameCharacter = Characters.OrkWithBigShoota
  val player2BigShootaUnitLocation: Coordinates = Coordinates(8, 10)
  val unit3: GameUnit = GameUnit(
    character = player2BigShootaUnit,
    coordinates = player2BigShootaUnitLocation,
    state = ALIVE_STATE
  )
  val player2BigChoppaUnit: GameCharacter = Characters.OrkWithBigChoppa
  val player2BigChoppaUnitLocation: Coordinates = Coordinates(10, 10)
  val unit4: GameUnit = GameUnit(
    character = player2BigChoppaUnit,
    coordinates = player2BigChoppaUnitLocation,
    state = ALIVE_STATE
  )
  val player2SkorchaUnit: GameCharacter = Characters.OrkWithScorcha
  val player2SkorchaUnitLocation: Coordinates = Coordinates(1, 10)
  val unit5: GameUnit = GameUnit(
    character = player2SkorchaUnit,
    coordinates = player2SkorchaUnitLocation,
    state = ALIVE_STATE
  )


  val player1Units: List[GameUnit] = List(unit1)
  val player2Units: List[GameUnit] = List(unit2, unit3, unit4, unit5)
  val mapWeAreUsing = Maps.RockyDivide
  val isPlayer1First = true


  val george = new RangeAttackManager2
  val rangeAttackManager = new RangeAttackManager2
  val closeCombatManager = new CloseCombatManager2
  val graveYardManager = new GraveYardManager
  val movementManager = new MovementManager
  val victoryChecker = new CheckVictoryConditions

//  val cache: IO[Cache[IO, String, Board]] = CaffeineCache[IO, String, Board]
//
//  implicit val caffeineCache: Cache[IO, String, Board] = cache.unsafeRunSync()


  start()


  @cask.get("/start/")
  def start(): String = {
    "Would you like to start the game (y/n)?"
  }

  @cask.post("/start/")
  def generateBoardString(request: Request): String = {
    val userInput = request.text().trim.toLowerCase
    if (userInput == "y") {
      // Generate a unique boardId using UUID
      //      val boardId = UUID.randomUUID().toString
      val boardId = "123"
      // Call the method to generate the board string
      val board = Board(
        boardId = boardId,
        player1 = player1Units.map(_.character),
        player2 = player2Units.map(_.character),
        map = mapWeAreUsing,
        isMovePhase = false,
        isShootingPhase = false,
        isCloseCombatPhase = false,
        isTopOfTurn = true,
        isPlayer1Turn = true,
        turnNumber = 0
      )
      // Print the board directly
      board.printBoard()
      // Cache the board
      sync.put(boardId)(board)
      Thread.sleep(100) // Add a brief delay
//
      // Retrieve the cached board
      Thread.sleep(100) // Add a brief delay
//      val cachedBoard: Option[Board] = get[IO, String, Board](boardId).unsafeRunSync()

      // Return a success message
      s"Board generated with boardId: $boardId and cached.\n${board.printBoard()}"    } else if (userInput == "n") {
      // Return a message indicating the user opted not to start the game
      "Game not started. Exiting..."
    } else {
      // Return a message indicating invalid input
      "Invalid input. Please enter 'y' or 'n'."
    }
  }

  @cask.post("/move")
  def move(): String = {
    movementManager.httpMove(Coordinates(1, 1), "123")
  }





  initialize()
}








// curl http://localhost:8080/start/
// curl -X POST http://localhost:8080/start/ -d "y"
// curl -X POST http://localhost:8080/move