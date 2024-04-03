package warhammer.http


import cask.model.Request
import warhammer.game.models.{Board, Coordinates}
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods.parse
import scalacache.modes.sync.mode
import scalacache.{Cache, sync}
import warhammer.game.MovementManager

case class MoveRoute(movementManager: MovementManager)(implicit cc: castor.Context,
                                                       log: cask.Logger,
                                                       cache: Cache[Board]) extends cask.Routes {
  implicit val formats: DefaultFormats.type = DefaultFormats

  @cask.post("/move/:boardId")
  def move(request: Request, boardId: String): String = {
    // Retrieve the cached board using the boardId
    val cachedBoard: Option[Board] = sync.get(boardId)
    cachedBoard match {
      case Some(board) =>
        if (!board.isMovePhase) {
          // If it's not the move phase, return a message indicating that move actions are not allowed
          s"Move actions are not allowed in the current phase. Current phase: ${board.getCurrentPhase(board)}"
        } else {
          // Parse JSON data from the request body and print it for debugging
          val requestBody = request.text()
          println(s"Request Body: $requestBody")

          // Parse the JSON data and extract the fields
          val json = parse(requestBody)
          val toCoordinates = (json \ "toCoordinates").extract[Coordinates]
          val avatar = (json \ "avatar").extract[String]

          // Perform further processing using toCoordinates and avatar
          val (updatedBoard, moveResult) = movementManager.httpMove(toCoordinates, board, avatar)
          val updatedBoardWithPhase = updatedBoard.phaseManager
          sync.put(boardId)(updatedBoardWithPhase) // Update the cached board
          s"$moveResult\nPhase transition completed. Current phase: ${board.getCurrentPhase(updatedBoardWithPhase)}"
        }
      case None =>
        // If the board with the provided boardId is not found, return an error message
        "Board not found. Please provide a valid boardId."
    }


  }

  initialize()
}