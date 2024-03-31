package warhammer.http

import cask.model.Request
import warhammer.http.models.StartGameRequest
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods.parse
import scalacache.modes.sync.mode
import scalacache.{Cache, sync}
import warhammer.game.models.{Board, GameInitializer, Maps}
import java.util.UUID


case class StartRoutes()(implicit cc: castor.Context,
                         log: cask.Logger,
                         cache: Cache[Board]) extends cask.Routes {
  implicit val formats: DefaultFormats.type = DefaultFormats
  val (player1Units, player2Units) = GameInitializer.initializeGameUnits()
  val mapWeAreUsing = Maps.RockyDivide

  @cask.get("/start")
  def start(): String = {
    "Would you like to start the game (y/n)?"
  }


  @cask.post("/start")
  def jStart(request: Request): String = {
    // Parse JSON data from the request body and print it for debugging
    val requestBody = request.text()
    println(s"Request Body: $requestBody")

    // Parse the JSON data and extract the field
    val json = parse(requestBody)
    val startGameRequest = json.extract[StartGameRequest]

    // Perform further processing based on the extracted field
    val userInput = startGameRequest.start.trim.toLowerCase
    if (userInput == "y") {
      // Generate a unique boardId using UUID
      val boardId = UUID.randomUUID().toString
      // Call the method to generate the board string
      val board = Board(
        boardId = boardId,
        player1 = player1Units.map(_.character),
        player2 = player2Units.map(_.character),
        map = mapWeAreUsing,
        isMovePhase = true,
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

      // Return a success message
      s"Board generated with boardId: $boardId and cached.\n${board.printBoard()}"
    } else if (userInput == "n") {
      // Game not started logic here
      "Game not started. Exiting..."
    } else {
      // Invalid input logic here
      "Invalid input. Please enter 'y' or 'n'."
    }
  }

  initialize()
}
