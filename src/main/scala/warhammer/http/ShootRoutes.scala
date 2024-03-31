package warhammer.http


import cask.model.Request
import warhammer.game.models.{Board, Coordinates}
import warhammer.http.models.ActionRequest
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods.parse
import scalacache.modes.sync.mode
import scalacache.{Cache, sync}
import warhammer.game.{CheckVictoryConditions, RangeAttackMangerHttp}

import scala.util.Try

case class ShootRoutes(rangeAttackManager: RangeAttackMangerHttp, victoryChecker: CheckVictoryConditions,boardId: String)(implicit cc: castor.Context,
                                                                                                           log: cask.Logger,
                                                                                                           cache: Cache[Board]) extends cask.Routes{
  implicit val formats: DefaultFormats.type = DefaultFormats

  @cask.get("/shoot")
  def checkShooter(request: Request): String = {
//    val boardId = "123" // Assuming the boardId is fixed for now
    // Retrieve the cached board using the boardId
    val cachedBoard: Option[Board] = sync.get(boardId)
    cachedBoard match {
      case Some(board) =>
        if (!board.isShootingPhase) {
          // If it's not the shoot phase, return a message indicating that shoot actions are not allowed
          s"Shoot actions are not allowed in the current phase. Current phase: ${board.getCurrentPhase(board)}"
        } else {
          // Your shoot phase logic here
          val activeUnitsAndTargets = rangeAttackManager.getActiveUnitsAndTargets(board)
          val unitsWithRangeTargets = activeUnitsAndTargets.filter { case (_, targets) =>
            targets.nonEmpty
          }

          // Construct the message indicating the units with targets
          val targetsMessage = unitsWithRangeTargets.map { case (unit, targets) =>
            s"${unit.avatar} has targets: ${targets.map(t => s"${t.avatar} at (${t.currentPosition.x}, ${t.currentPosition.y})").mkString(", ")}"
          }.mkString("\n")

          // If there are no active players with targets, return the corresponding message
          if (unitsWithRangeTargets.isEmpty) {
            "No enemies in line of sight"
          }

          // Update shootingPhaseCompleted for units with empty targets
          val unitsWithEmptyRangeTargets = activeUnitsAndTargets.filter { case (_, targets) =>
            targets.isEmpty
          }.keys.toList

          val updatedUnits = unitsWithEmptyRangeTargets.map { unit =>
            unit.copy(shootingPhaseCompleted = true)
          }

          // Update the board with the units that have completed their shooting phase
          val updatedBoardWithCompletedUnits = (board.getActiveDeadPlayers ++ updatedUnits).foldLeft(board) { (currentBoard, updatedUnit) =>
            currentBoard.updateActiveUnit(updatedUnit)
          }

          // Run phaseManager on the updated board
          val updatedBoardWithPhase = updatedBoardWithCompletedUnits.phaseManager

          // Construct the message indicating the phase transition
          val phaseTransitionMessage = s"Phase transition completed. Current phase: ${board.getCurrentPhase(updatedBoardWithPhase)}"

          // Update the cached board with the phase transition
          sync.put(boardId)(updatedBoardWithPhase)

          // Combine the messages
          val combinedMessage = s"$targetsMessage\n$phaseTransitionMessage"

          // Return the combined message
          combinedMessage
        }
      case None =>
        // If the board with the provided boardId is not found, return an error message
        "Board not found. Please provide a valid boardId."
    }
  }


  @cask.post("/shoot")
  def jshoot(request: Request): String = {
    val json = parse(request.text())
    val actionRequest = json.extract[ActionRequest]
//    val boardId = "123" // Assuming the boardId is fixed for now
    val avatar = actionRequest.avatar
    val coordinatesOption = for {
      x <- Try(actionRequest.x.toInt).toOption
      y <- Try(actionRequest.y.toInt).toOption
    } yield Coordinates(x, y)

    val result = coordinatesOption match {
      case Some(coords) =>
        val cachedBoard: Option[Board] = sync.get(boardId)
        cachedBoard match {
          case Some(board) =>
            if (!board.isShootingPhase) {
              // If it's not the shoot phase, return a message indicating that shoot actions are not allowed
              s"Shoot actions are not allowed in the current phase. Current phase: ${board.getCurrentPhase(board)}"
            } else {
              val (updatedBoard, attackResult) = rangeAttackManager.performRangedAttackHttp(avatar, coords, board)
              // Perform phase transition
              val updatedBoardWithPhase = updatedBoard.phaseManager

              sync.put(boardId)(updatedBoardWithPhase) // Update the cached board
              // Print the updated board to local host terminal
              println("Updated board:")
              val victoryMessage = victoryChecker.checkVictory(updatedBoard.getActivePlayers, updatedBoard.getPassivePlayers)

              // Print both attack result, victory message (if available), and phase transition message
              val phaseTransitionMessage = s"Phase transition completed. Current phase: ${board.getCurrentPhase(updatedBoardWithPhase)}"
              println(s"Attack result: $attackResult")
              victoryMessage.foreach { message =>
                println(s"Victory message: $message")
              }

              println(phaseTransitionMessage)
              // Return the combined message
              s"$attackResult\n$victoryMessage\n$phaseTransitionMessage"
            }
          case None =>
            "Board not found. Please provide a valid boardId."
        }
      case None =>
        "Invalid input provided. Please provide coordinates and an avatar separated by a space."
    }

    result
  }

  initialize()
}
