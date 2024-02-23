//package jobs
//
//import cats.effect.IO
//import models.{Board, Coordinates}
//import scala.io.StdIn
//import scala.collection.mutable.Queue
//import cats.effect.unsafe.implicits.global
//import cats.effect.IO
//import scalacache._
//import scalacache.caffeine._
//import cask.endpoints.get
//
//class MoveHttpManager (cache: IO[Cache[IO, String, Board]]){
//
//
//  def httpMove(move: Coordinates, boardId: String): String = {
//    // Retrieve the cached board using the provided boardId
//    val cachedBoard: IO[Option[Board]] = cache.flatMap(_.get(boardId))
//
//    // Evaluate the IO action
//    val result: Option[Board] = cachedBoard.unsafeRunSync()
//
//    result match {
//      case Some(board) =>
//        // Get the active player's units from the board
//        val activePlayerUnits = if (board.isPlayer1Turn) board.player1Characters else board.player2Characters
//
//        // Find the unit belonging to the active player at the specified coordinates
//        val unitToUpdate = activePlayerUnits.find(_.currentPosition == move)
//
//
//        unitToUpdate match {
//          case Some(unit) =>
//            // Check if the move is valid
//            val isValidMove = isValidMove(board.map, move, unit, getPassivePlayers(board, unit))
//
//            if (isValidMove) {
//              // Update the character's coordinates with the new move
//              val updatedUnit = unit.copy(coordinates = move)
//
//              // Create a new board with the updated character's coordinates and the same boardId
//              val updatedBoard = board.copy(
//                player1Units = if (board.isPlayer1Turn) updateUnit(board.player1Units, updatedUnit) else board.player1Units,
//                player2Units = if (!board.isPlayer1Turn) updateUnit(board.player2Units, updatedUnit) else board.player2Units
//              )
//
//              // Cache the updated board
//              cache.flatMap(_.put(boardId)(updatedBoard)).unsafeRunSync()
//
//              // Return a success message
//              s"Successfully moved ${unit.character.avatar} to coordinates: $move"
//            } else {
//              // If the move is not valid, return an error message
//              "Invalid move. Please choose a valid move."
//            }
//          case None =>
//            // If no unit is found at the specified coordinates, return an error message
//            "No unit found at the specified coordinates."
//        }
//      case None =>
//        // If the board with the provided boardId is not found in the cache, return an error message
//        "Board not found. Please provide a valid boardId."
//    }
//  }
//}
