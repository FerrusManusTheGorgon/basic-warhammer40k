package warhammer.game.models

import UnitState.{ALIVE_STATE, DEAD_STATE}



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

  def updateActiveUnits(updatedCharacters: List[GameCharacter]): Board = {
    if (isPlayer1Turn) {
      this.copy(player1 = updatedCharacters)
    } else {
      this.copy(player2 = updatedCharacters)
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

  def getPassiveAlivePlayers: List[GameCharacter] = {
    this.getPassivePlayers.filter(_.state == ALIVE_STATE)
  }

  def getPassiveDeadPlayers: List[GameCharacter] = {
    this.getPassivePlayers.filter(_.state == DEAD_STATE)
  }

  def getActivePlayers: List[GameCharacter] = {
    if (this.isPlayer1Turn) {
      this.player1
    } else {
      this.player2
    }
  }

  def getActiveAlivePlayers: List[GameCharacter] = {
    this.getActivePlayers.filter(_.state == ALIVE_STATE)
  }

  def getActiveDeadPlayers: List[GameCharacter] = {
    this.getActivePlayers.filter(_.state == DEAD_STATE)
  }

  def getAllPlayers: List[GameCharacter] = {
    getActivePlayers ++ getPassivePlayers
  }

  def getAllAlivePlayers: List[GameCharacter] = {
    this.getAllPlayers.filter(_.state == ALIVE_STATE)
  }

  def getAllDeadPlayers: List[GameCharacter] = {
    this.getAllPlayers.filter(_.state == DEAD_STATE)
  }

  def getCurrentPhase(board: Board): String = {
    if (board.isMovePhase) "Move"
    else if (board.isShootingPhase) "Shoot"
    else if (board.isCloseCombatPhase) "Assault"
    else "Unknown"
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
      boardState(y - 1)(x - 1) = if (character.state == ALIVE_STATE) character.avatar else "x"
    }

    // Place player 2 characters on the board
    player2.foreach { character =>
      val Coordinates(x, y) = character.currentPosition
      boardState(y - 1)(x - 1) = if (character.state == ALIVE_STATE) character.avatar else "x"
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
    activeUnits.filter(_.state == ALIVE_STATE).forall(_.movePhaseCompleted)
  }

  private def allUnitsShot(activeUnits: List[GameCharacter]): Boolean = {
    activeUnits.filter(_.state == ALIVE_STATE).forall(_.shootingPhaseCompleted)
  }

  private def allUnitsAssaulted(activeUnits: List[GameCharacter]): Boolean = {
    activeUnits.filter(_.state == ALIVE_STATE).forall(_.closeCombatPhaseCompleted)
  }


  // Method to update the phase based on the completion statuses of active units
  def phaseManager: Board = {
    val activeUnits = if (isPlayer1Turn) player1 else player2
    val aliveActiveUnits = activeUnits.filter(_.state == "alive")
    val isMovePhaseComplete = allUnitsMoved(aliveActiveUnits)
    val isShootingPhaseComplete = allUnitsShot(aliveActiveUnits)
    val isCloseCombatPhaseComplete = allUnitsAssaulted(aliveActiveUnits)

    (isMovePhaseComplete, isShootingPhaseComplete, isCloseCombatPhaseComplete) match {
      case (true, false, false) => handleEndOfMovementPhase
      case (false, true, false) => handleEndOfShootingPhase
      case (false, false, true) => handleEndOfCloseCombatPhase
      case (false, false, false) => this
    }


    // Update the phase flags first
    //    val updatedBoardWithPhases = this.copy(
    //      isMovePhase = newIsMovePhase,
    //      isShootingPhase = newIsShootingPhase,
    //      isCloseCombatPhase = newIsCloseCombatPhase
    //    )

    // Transition from close combat phase back to move phase and switch player turns
    //    val updatedBoardWithTurn = if (newIsMovePhase) {
    //      updatedBoardWithPhases.copy(
    //        isPlayer1Turn = !isPlayer1Turn,
    //        turnNumber = turnNumber + 1
    //      )
    //    } else {
    //      updatedBoardWithPhases
    //    }

    // Reset phaseCompleted flags based on the phase transition
    //    val updatedActiveUnits = activeUnits.map { character =>
    //      character.copy(
    //        movePhaseCompleted = newIsShootingPhase,
    //        shootingPhaseCompleted = newIsCloseCombatPhase,
    //        closeCombatPhaseCompleted = newIsMovePhase
    //      )
    //    }

    // Update the board with updated active units
    //    updatedBoardWithTurn.copy(
    //      player1 = if (isPlayer1Turn) updatedActiveUnits else player1,
    //      player2 = if (!isPlayer1Turn) updatedActiveUnits else player2
    //    )
  }

  def handleEndOfMovementPhase: Board = {
    val updatedCharacters = this.getActivePlayers.map(character => character.copy(movePhaseCompleted = false))
    this.updateActiveUnits(updatedCharacters).copy(
      isMovePhase = false,
      isShootingPhase = true
    )
  }

  def handleEndOfShootingPhase: Board = {
    val updatedCharacters = this.getActivePlayers.map(character => character.copy(shootingPhaseCompleted = false))
    this.updateActiveUnits(updatedCharacters).copy(
      isShootingPhase = false,
      isCloseCombatPhase = true
    )
  }

  def handleEndOfCloseCombatPhase: Board = {
    val updatedCharacters = this.getActivePlayers.map(character => character.copy(closeCombatPhaseCompleted = false))
    val turnNumber = if (this.isTopOfTurn) this.turnNumber else this.turnNumber + 1
    this.updateActiveUnits(updatedCharacters).copy(
      isCloseCombatPhase = false,
      isMovePhase = true,
      turnNumber = turnNumber,
      isPlayer1Turn = !this.isPlayer1Turn,
      isTopOfTurn = !this.isTopOfTurn
    )
  }



}






