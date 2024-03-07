package models

import models.{Coordinates, MapConfig}
import models.{Characters, GameCharacter}

import scala.Predef.->


case class Board(
                  boardId: String,
                  player1: List[GameCharacter],
                  player2: List[GameCharacter],
                  map: MapConfig,
                  isMovePhase: Boolean,
                  isShootingPhase: Boolean,
                  isCloseCombatPhase: Boolean,
                  isTopOfTurn: Boolean,
                  isPlayer1Turn: Boolean,
                  turnNumber: Int
                ) {


  def updateActiveUnit(updatedCharacter: GameCharacter): Board = {
    if (isPlayer1Turn) {
      val newCharacters = this.player1.filterNot(_.characterId == updatedCharacter.characterId) :+ updatedCharacter
      this.copy(player1 = newCharacters)
    } else {
      val newCharacters = this.player2.filterNot(_.characterId == updatedCharacter.characterId) :+ updatedCharacter
      this.copy(player2 = newCharacters)
    }
  }

  def updatePassiveUnit(updatedCharacter: GameCharacter): Board = {
    if (!isPlayer1Turn) {
      val newCharacters = this.player1.filterNot(_.characterId == updatedCharacter.characterId) :+ updatedCharacter
      this.copy(player1 = newCharacters)
    } else {
      val newCharacters = this.player2.filterNot(_.characterId == updatedCharacter.characterId) :+ updatedCharacter
      this.copy(player2 = newCharacters)
    }
  }


  def getPassivePlayers: List[GameCharacter] = {
    if (this.isPlayer1Turn) {
      this.player2
    } else {
      this.player1
    }
  }

  def getActivePlayers: List[GameCharacter] = {
    if (this.isPlayer1Turn) {
      this.player1
    } else {
      this.player2
    }
  }

  //  def print: String = {
  //
  //    "XXXXXX"
  //  }

  def printBoard(): String = {
//    println(s"${this.player1.mkString(" ")}")
    val boardState = Array.fill(map.verticalLength, map.horizontalLength)(map.EMPTY_SQUARE)
//    println(s"${this.player1.mkString(" ")}")
    // Place player 1 characters on the board
    player1.foreach { character =>
      val Coordinates(x, y) = character.currentPosition
      boardState(y - 1)(x - 1) = character.avatar
    }

    // Place player 2 characters on the board
    player2.foreach { character =>
      val Coordinates(x, y) = character.currentPosition
      boardState(y - 1)(x - 1) = character.avatar
    }
    // Place blockers on the board
    map.blocker.foreach { coord =>
      val Coordinates(x, y) = coord
      boardState(y - 1)(x - 1) = map.BLOCKED_SQUARE
    }

    // Convert the board state to a string
    val boardString = new StringBuilder
    boardString.append(map.HORIZONTAL_BORDER).append("\n")
    for (y <- map.VERTICAL_RANGE.reverse) {
      boardString.append(f"$y%4d  ")
      for (x <- map.HORIZONTAL_RANGE) {
        boardString.append("|  ").append(boardState(y - 1)(x - 1)).append("  ")
      }
      boardString.append("|\n").append(map.HORIZONTAL_BORDER).append("\n")
    }
    boardString.append("      ").append(map.HORIZONTAL_RANGE.map(x => f"$x%4d").mkString("  "))

    boardString.toString()
  }

  // Function to update the phase of a board
  def updatePhase(isMovePhase: Boolean, isShootingPhase: Boolean, isCloseCombatPhase: Boolean): Board =
    this.copy(
      isMovePhase = isMovePhase,
      isShootingPhase = isShootingPhase,
      isCloseCombatPhase = isCloseCombatPhase
    )


  // Method to check if all active units have completed their move phase
  private def allUnitsMoved(activeUnits: List[GameCharacter]): Boolean = {
    activeUnits.forall(_.movePhaseCompleted)
  }

  // Method to check if all active units have completed their shooting phase
  private def allUnitsShot(activeUnits: List[GameCharacter]): Boolean = {
    activeUnits.forall(_.shootingPhaseCompleted)
  }

  // Method to check if all active units have completed their close combat phase
  private def allUnitsAssaulted(activeUnits: List[GameCharacter]): Boolean = {
    activeUnits.forall(_.closeCombatPhaseCompleted)
  }

  // Method to update the phase based on the completion statuses of active units
  def phaseManager: Board = {
    val activeUnits = if (isPlayer1Turn) player1 else player2
    val newIsMovePhase = !allUnitsMoved(activeUnits) && !isShootingPhase && !isCloseCombatPhase
    val newIsShootingPhase = allUnitsMoved(activeUnits) && !allUnitsShot(activeUnits) && !isCloseCombatPhase
    val newIsCloseCombatPhase = allUnitsShot(activeUnits) && !allUnitsAssaulted(activeUnits) && !isMovePhase

    // Update the phase flags first
    val updatedBoardWithPhases = this.copy(
      isMovePhase = newIsMovePhase,
      isShootingPhase = newIsShootingPhase,
      isCloseCombatPhase = newIsCloseCombatPhase
    )

//    // Reset phaseCompleted flags based on the phase transition
//    val updatedActiveUnits = activeUnits.map { character =>
//      character.copy(
//        movePhaseCompleted = newIsMovePhase,
//        shootingPhaseCompleted = newIsShootingPhase,
//        closeCombatPhaseCompleted = newIsCloseCombatPhase
//      )
//    }

    // Update the board with updated active units
    updatedBoardWithPhases.copy(
//      player1 = if (isPlayer1Turn) updatedActiveUnits else player1,
//      player2 = if (!isPlayer1Turn) updatedActiveUnits else player2
    )
  }


  //  def getCurrentPhase(board: Board): String = {
//    if (board.isMovePhase) "Move"
//    else if (board.isShootingPhase) "Shoot"
//    else if (board.isCloseCombatPhase) "Assault"
//    else "Unknown"
//  }

}




