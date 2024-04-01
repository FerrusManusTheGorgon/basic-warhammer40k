package warhammer.http


import cask.model.Request
import warhammer.game.models.{Board, Coordinates}
import warhammer.http.models.ActionRequest
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods.parse
import scalacache.modes.sync.mode
import scalacache.{Cache, sync}
import warhammer.game.{CheckVictoryConditions, CloseCombatManager2Http}

import scala.util.Try
case class AssaultRoutes(closeCombatManager: CloseCombatManager2Http, victoryChecker: CheckVictoryConditions)(implicit cc: castor.Context,
                                                                                                              log: cask.Logger,
                                                                                                              cache: Cache[Board]) extends cask.Routes{
  implicit val formats: DefaultFormats.type = DefaultFormats

  @cask.get("/assault/:boardId")
  def assaulty(request: Request, boardId: String): String = {
    // Retrieve the cached board using the boardId
    val cachedBoard: Option[Board] = sync.get(boardId)
    cachedBoard match {
      case Some(board) =>
        if (!board.isCloseCombatPhase) {
          // If it's not the shoot phase, return a message indicating that shoot actions are not allowed
          s"Assault actions are not allowed in the current phase. Current phase: ${board.getCurrentPhase(board)}"
        } else {
          val activeUnitsAndAssaultTargets = closeCombatManager.getActiveUnitsAndAssaultTargets(board)
          val unitsWithAssaultTargets = activeUnitsAndAssaultTargets.filter { case (_, targets) =>
            targets.nonEmpty
          }

          // If there are no active players with targets, return the corresponding message
          if (unitsWithAssaultTargets.isEmpty)
            "No enemies in Assault Range"

          // Construct the message indicating the units with targets
          val targetsMessage = unitsWithAssaultTargets.map { case (unit, targets) =>
            s"${unit.avatar} has targets: ${targets.map(t => s"${t.avatar} at (${t.currentPosition.x}, ${t.currentPosition.y})").mkString(", ")}"
          }.mkString("\n")

          // Update shootingPhaseCompleted for units with empty targets
          val unitsWithEmptyAssaultTargets = activeUnitsAndAssaultTargets.filter { case (_, targets) =>
            targets.isEmpty
          }.keys.toList

          val updatedAssaultUnits = unitsWithEmptyAssaultTargets.map { unit =>
            unit.copy(closeCombatPhaseCompleted = true)
          }

          // Update the board with the units that have completed their shooting phase
          val updatedBoardWithCompletedUnits = updatedAssaultUnits.foldLeft(board) { (currentBoard, updatedUnit) =>
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


  @cask.post("/jassault/:boardId")
  def jassault(request: Request, boardId: String): String = {
    val actionRequestO = for {
      json <- Try(parse(request.text())).toOption
      actionRequest <- Try(json.extract[ActionRequest]).toOption
    } yield actionRequest

    val result = actionRequestO match {
      case Some(actionRequest: ActionRequest) =>
        val avatar = actionRequest.avatar
        val coords = actionRequest.toCoordinates
        val cachedBoard: Option[Board] = sync.get(boardId)
        cachedBoard match {
          case Some(board) =>
            if (!board.isCloseCombatPhase) {
              // If it's not the assault phase, return a message indicating that assault actions are not allowed
              s"Assault actions are not allowed in the current phase. Current phase: ${board.getCurrentPhase(board)}"
            } else {
              // Perform the assault action using the avatar and coordinates
              val targetedCharacters = closeCombatManager.checkCloseCombatAttackHttp(board)
              val (updatedBoard, assaultResult) = closeCombatManager.performCloseCombatAttackHttp(board.map, board.getActivePlayers.head, coords, targetedCharacters, board)
              // Perform phase transition
              val updatedBoardWithPhase = updatedBoard.phaseManager
              sync.put(boardId)(updatedBoardWithPhase) // Update the cached board
              // Check for victory conditions
              val victoryMessage = victoryChecker.checkVictory(updatedBoard.getActivePlayers, updatedBoard.getPassivePlayers)
              // Construct phase transition message
              val phaseTransitionMessage = s"Phase transition completed. Current phase: ${board.getCurrentPhase(updatedBoardWithPhase)}"
              // Print the result to the console
              println(s"Assault result: $assaultResult")
              victoryMessage.foreach { message =>
                println(s"Victory message: $message")
              }
              println(phaseTransitionMessage)
              // Return the combined message
              s"$assaultResult\n$victoryMessage\n$phaseTransitionMessage"
            }
          case None =>
            "Board not found. Please provide a valid boardId."
        }
      case None =>
        "Invalid input provided. Please provide coordinates and an avatar in json."
    }

    result
  }


  initialize()
}