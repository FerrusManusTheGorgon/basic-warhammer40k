package warhammer.game

import warhammer.game.models.UnitState.{ALIVE_STATE, DEAD_STATE}
import warhammer.game.models.{Board, Coordinates, GameCharacter, MapConfig}
import scalacache.modes.sync._
import scalacache._

import scala.io.StdIn
import scala.util.Random


class RangeAttackMangerHttp(implicit cache: Cache[Board]) {


  // Method to check if a cell is blocked
  private def isCellBlocked(mapConfig: MapConfig, cell: Coordinates): Boolean = {
    mapConfig.layout.getOrElse(cell, "") == "BLOCKED_SQUARE"
  }

  // Method to print opponent in range
  private def printOpponentInRange(opponent: String, coordinates: Coordinates): Unit = {
    println(s"Opponent's character $opponent is in range at coordinates $coordinates")
  }

  @scala.annotation.tailrec
  final def performRangedAttack(mapConfig: MapConfig, activePlayer: GameCharacter, potentialTargets: List[GameCharacter]): GameCharacter = {
    // Display the potential targets
    println("Potential targets:")

    potentialTargets.foreach { target =>
      println(s"-${activePlayer.avatar} ${activePlayer.name} has ${target.avatar} ${target.name} at coordinates (${target.currentPosition.x}, ${target.currentPosition.y})in range and line of sight")
    }

    println("Enter the avatar of the character you want to attack or 'Hold Fire'")
    val input: String = StdIn.readLine().toLowerCase.trim

    if (input == "hold fire") {
      return activePlayer
    }

    // Find the potential target matching the input avatar
    val target = potentialTargets.find(_.avatar.toLowerCase == input)

    target match {
      case Some(passivePlayer) =>
        // Randomly determine if the attack hits based on the attacker's ballistic skill
        val attackerBS = activePlayer.ballisticSkill
        val randomChance = Random.nextInt(100) + 1
        println(s"$randomChance vs $attackerBS")

        val hitMessage = activePlayer.rangedAttackHitMessage
        val missMessage = activePlayer.rangedAttackMissMessage

        if (randomChance <= attackerBS) {
          println(hitMessage + s"${passivePlayer.avatar} at coordinates (${passivePlayer.currentPosition.x}, ${passivePlayer.currentPosition.y})!")
          // Update the passive player's state to "dead" and return the updated GameUnit
          val updatedPassivePlayer = passivePlayer.copy(state = DEAD_STATE, avatar = "")
          println("Updated passive player after attack:")
          println(updatedPassivePlayer)
          updatedPassivePlayer
        } else {
          println(missMessage + s"${passivePlayer.avatar} at coordinates (${passivePlayer.currentPosition.x}, ${passivePlayer.currentPosition.y})!")
          passivePlayer // Return the original GameUnit
        }

      case None =>
        println("Invalid target avatar. Please enter a valid avatar or 'Hold Fire'.")
        performRangedAttack(mapConfig, activePlayer, potentialTargets)
    }
  }

  def checkRangedAttack(mapConfig: MapConfig, activePlayer: GameCharacter, passivePlayers: List[GameCharacter], activePlayers: List[GameCharacter]): List[GameCharacter] = {
    println(s"Checking ranged attack for ${activePlayer.name}...")
    println(activePlayer)

    val effectiveRange = math.min(activePlayer.range, math.max(mapConfig.horizontalLength, mapConfig.verticalLength))
    val row = activePlayer.currentPosition.x
    val col = activePlayer.currentPosition.y

    // Filter out passive units that are not in the ALIVE_STATE
    val alivePassivePlayers = passivePlayers.filter(_.state == ALIVE_STATE)

    // Create a list to store coordinates and their contents
    var coordinatesAndContents: List[(Coordinates, String)] = List.empty

    // Create a list to store targetable coordinates
    var targetCoordinates: List[Coordinates] = List.empty

    // Check vertically above within effective range
    var verticalBlocked = false
    for (dx <- 1 to effectiveRange if !verticalBlocked) {
      val rowAbove = row + dx
      if (rowAbove < mapConfig.verticalLength + 1) {
        val coord = Coordinates(rowAbove, col)
        targetCoordinates ::= coord // Store the coordinate directly
        val content = mapConfig.layout.getOrElse(coord, "")
        coordinatesAndContents ::= (coord, content)
        println(s"Targetable cell at (${coord.x}, ${coord.y}) - Content: $content")
        if (mapConfig.blocker.contains(coord)) {
          println(s"Blocked coordinate at (${coord.x}, ${coord.y})")
          coordinatesAndContents = coordinatesAndContents.takeWhile { case (c, _) => c != coord }
          verticalBlocked = true
        }
      }
    }

    // Check vertically below within effective range
    var verticalBelowBlocked = false
    for (dx <- 1 to effectiveRange if !verticalBelowBlocked) {
      val rowBelow = row - dx
      if (rowBelow >= 0) {
        val coord = Coordinates(rowBelow, col)
        targetCoordinates ::= coord // Store the coordinate directly
        val content = mapConfig.layout.getOrElse(coord, "")
        coordinatesAndContents ::= (coord, content)
        println(s"Targetable cell at (${coord.x}, ${coord.y}) - Content: $content")
        if (mapConfig.blocker.contains(coord)) {
          println(s"Blocked coordinate at (${coord.x}, ${coord.y})")
          coordinatesAndContents = coordinatesAndContents.takeWhile { case (c, _) => c != coord }
          verticalBelowBlocked = true
        }
      }
    }

    // Check horizontally left within effective range
    var horizontalLeftBlocked = false
    for (dy <- 1 to effectiveRange if !horizontalLeftBlocked) {
      val colLeft = col - dy
      if (colLeft >= 0) {
        val coord = Coordinates(row, colLeft)
        targetCoordinates ::= coord // Store the coordinate directly
        val content = mapConfig.layout.getOrElse(coord, "")
        coordinatesAndContents ::= (coord, content)
        println(s"Targetable cell at (${coord.x}, ${coord.y}) - Content: $content")
        if (mapConfig.blocker.contains(coord)) {
          println(s"Blocked coordinate at (${coord.x}, ${coord.y})")
          coordinatesAndContents = coordinatesAndContents.takeWhile { case (c, _) => c != coord }
          horizontalLeftBlocked = true
        }
      }
    }

    // Check horizontally right within effective range
    var horizontalRightBlocked = false
    for (dy <- 1 to effectiveRange if !horizontalRightBlocked) {
      val colRight = col + dy
      if (colRight < mapConfig.horizontalLength + 1) {
        val coord = Coordinates(row, colRight)
        targetCoordinates ::= coord // Store the coordinate directly
        val content = mapConfig.layout.getOrElse(coord, "")
        coordinatesAndContents ::= (coord, content)
        println(s"Targetable cell at (${coord.x}, ${coord.y}) - Content: $content")
        if (mapConfig.blocker.contains(coord)) {
          println(s"Blocked coordinate at (${coord.x}, ${coord.y})")
          coordinatesAndContents = coordinatesAndContents.takeWhile { case (c, _) => c != coord }
          horizontalRightBlocked = true
        }
      }
    }

    // Filter out cells that are immediately adjacent to the active player's cell
    targetCoordinates = targetCoordinates.filterNot { coord =>
      val (x, y) = (coord.x, coord.y)
      val activeX = activePlayer.currentPosition.x
      val activeY = activePlayer.currentPosition.y

      (x == activeX && y == activeY + 1) || // Exclude cell to the right
        (x == activeX && y == activeY - 1) || // Exclude cell to the left
        (y == activeY && x == activeX + 1) || // Exclude cell below
        (y == activeY && x == activeX - 1) // Exclude cell above
    }

    // Filter out cells that are blocked or contain active players
    targetCoordinates = targetCoordinates.filterNot { coord =>
      val blockedOrActive = mapConfig.blocker.contains(coord) || activePlayers.exists { activePlayer =>
        activePlayer.state == ALIVE_STATE && activePlayer.currentPosition == coord
      }
      if (blockedOrActive) {
        println(s"Filtered coordinate at (${coord.x}, ${coord.y})")
      }
      blockedOrActive
    }

    // Now filter passive players based on targetable coordinates
    alivePassivePlayers.filter(passivePlayer =>
      targetCoordinates.contains(passivePlayer.currentPosition)
    )
  }



  // Method to get the current status of passive units, filtering out dead units
  def getCurrentPassiveUnitsStatus(passiveUnits: List[GameCharacter]): List[GameCharacter] = {
    // Filter out units that are not in the DEAD_STATE
    val alivePassiveUnits = passiveUnits.filter(_.state == ALIVE_STATE)
    println("Current passive units status:")
    alivePassiveUnits.foreach { unit =>
      println(s"- ${unit.avatar} at coordinates (${unit.currentPosition.x}, ${unit.currentPosition.y}), state: ${unit.state}")
    }
    alivePassiveUnits
  }


  // Method to perform ranged attacks if passive units are in range

  final def performRangedAttackIfInRange(activeUnits: List[GameCharacter], finishedUnits: List[GameCharacter], map: MapConfig, passiveUnits: List[GameCharacter]): List[GameCharacter] = {
    activeUnits match {
      case Nil => passiveUnits // No more units to process, return the units list
      case unit :: remainingUnits =>
        println(s"Processing unit: ${unit.avatar}") // Print the current unit being processed

        // Get the current status of passive units, filtering out dead units
        val currentPassiveUnits = getCurrentPassiveUnitsStatus(passiveUnits)

        //check if the current unit can perform a ranged attack
        val allActiveUnits = activeUnits ++ finishedUnits

        // Check if the current unit can perform a ranged attack
        checkRangedAttack(map, unit, currentPassiveUnits, allActiveUnits) match {
          case potentialTargets if potentialTargets.nonEmpty =>
            // If there are potential targets, print them
            println("Potential targets for attack:")
            potentialTargets.foreach { target =>
              println(s"- ${target.avatar} at coordinates (${target.currentPosition.x}, ${target.currentPosition.y}), state: ${target.state}")
            }

            // Perform the attack and update the units list accordingly
            val updatedUnit = performRangedAttack(map, unit, potentialTargets)

            // Filter out passive units that were eliminated during the attack
            val remainingPassiveUnits = currentPassiveUnits.map { passiveUnit =>
              if (passiveUnit.currentPosition == updatedUnit.currentPosition) updatedUnit else passiveUnit
            }.filter(_.state == ALIVE_STATE)


            // Print the current status of passive units after filtering
            println("Current passive units status:")
            remainingPassiveUnits.foreach { unit =>
              println(s"- ${unit.avatar} at coordinates (${unit.currentPosition.x}, ${unit.currentPosition.y}), state: ${unit.state}")
            }

            // Print the remaining passive units after filtering
            println("Remaining passive units after attack:")
            remainingPassiveUnits.foreach { unit =>
              println(s"- ${unit.avatar} at coordinates (${unit.currentPosition.x}, ${unit.currentPosition.y}), state: ${unit.state}")
            }

            // Continue processing the remaining units with the updated units list
            performRangedAttackIfInRange(remainingUnits, finishedUnits ++ List(unit), map, remainingPassiveUnits)


          case _ =>
            // If there are no potential targets, print a message and continue processing the remaining units
            println("No enemies in range or line of sight.")
            performRangedAttackIfInRange(remainingUnits, finishedUnits ++ List(unit), map, currentPassiveUnits)
        }
    }
  }

  //
  def rangeAttackHttpIfInRange(shootCoordinates: Coordinates, boardId: String, avatar: String): String = {
    val cachedBoard: Option[Board] = sync.get(boardId)
    cachedBoard match {
      case Some(board) =>
        // Get the active player's units from the board
        val activePlayerUnits = board.getActivePlayers
        val currentShootUnitOption = activePlayerUnits.find(p => p.avatar == avatar && !p.shootingPhaseCompleted)
        currentShootUnitOption match {
          case Some(currentShootUnit) =>
            if (shootCoordinates == Coordinates(100, 100)) {
              // Special case for coordinates (100, 100)
              val updatedUnit = currentShootUnit.copy(shootingPhaseCompleted = true)
              val updatedBoard = board.updateActiveUnit(updatedUnit)
              sync.put(boardId)(updatedBoard)
              updatedBoard.printBoard()
              s"$avatar held its fire"
            } else {
              // Check ranged attack for the current shoot unit
              checkRangedAttack(board.map, currentShootUnit, board.getPassivePlayers, activePlayerUnits) match {
                case Nil =>
                  s"No enemies in range or line of sight for ${currentShootUnit.avatar} at coordinates: $shootCoordinates"
                case targets =>
                  // If there are potential targets, print them
                  println("Potential targets for attack:")
                  targets.foreach { target =>
                    println(s"- ${target.avatar} at coordinates (${target.currentPosition.x}, ${target.currentPosition.y}), state: ${target.state}")
                    val updatedTarget = target.copy(avatar = " ", state = "dead")
                    board.updateActiveUnit(updatedTarget)
                  }

                  val updatedShootUnit = currentShootUnit.copy(shootingPhaseCompleted = true)
                  // Update the board with the modified targets
                  val updatedBoard = board.updateActiveUnit(updatedShootUnit)
                  sync.put(boardId)(updatedBoard)
                  updatedBoard.printBoard()
                  "Ranged attack performed successfully."
              }
            }
          case None =>
            "No units available for ranged attack."
        }
      case None =>
        // If the board with the provided boardId is not found in the cache, return an error message
        "Board not found. Please provide a valid boardId."
    }
  }


//  def checkRangedAttackHttp(board: Board, avatar: String): List[GameCharacter] = {
//    val currentShootUnitOption = board.getActiveAlivePlayers.find(p => !p.shootingPhaseCompleted && p.avatar == avatar).get
//    checkRangedAttack(board.map, currentShootUnitOption, board.getPassiveAlivePlayers, board.getActiveAlivePlayers)
//    //
//    //
//  }
def checkRangedAttackHttp(board: Board, avatar: String): List[GameCharacter] = {
  val currentShootUnitOption = board.getActiveAlivePlayers.find(p => !p.shootingPhaseCompleted && p.avatar == avatar)
  currentShootUnitOption match {
    case Some(currentShootUnit) =>
      checkRangedAttack(board.map, currentShootUnit, board.getPassiveAlivePlayers, board.getActiveAlivePlayers)
    case None =>
      // Handle the case where no matching player is found
      println("No active player found with the given avatar.")
      List.empty[GameCharacter] // Or any other appropriate action
  }
}


  // def performRangedAttackHttp(mapConfig: MapConfig, activePlayer: GameCharacter, targetCoordinates: Coordinates, potentialTargets: List[GameCharacter]): GameCharacter = {
  //    // Display the potential targets
  //
  //
  ////    println("Potential targets:")
  ////    potentialTargets.foreach { target =>
  ////      println(s"-${activePlayer.avatar} ${activePlayer.name} has ${target.avatar} ${target.name} at coordinates (${target.currentPosition.x}, ${target.currentPosition.y}) in range and line of sight")
  ////    }
  //
  //    // Find the potential target matching the input coordinates
  //    val target = potentialTargets.find(_.currentPosition == targetCoordinates)
  //
  //    target match {
  //      case Some(passivePlayer) =>
  //        // Randomly determine if the attack hits based on the attacker's ballistic skill
  //        val attackerBS = activePlayer.ballisticSkill
  //        val randomChance = Random.nextInt(100) + 1
  //        println(s"$randomChance vs $attackerBS")
  //
  //        val hitMessage = activePlayer.rangedAttackHitMessage
  //        val missMessage = activePlayer.rangedAttackMissMessage
  //
  //        if (randomChance <= attackerBS) {
  //          println(hitMessage + s"${passivePlayer.avatar} at coordinates (${passivePlayer.currentPosition.x}, ${passivePlayer.currentPosition.y})!")
  //          // Update the passive player's state to "dead" and return the updated GameUnit
  //          val updatedPassivePlayer = passivePlayer.copy(state = DEAD_STATE, avatar = "")
  //          println("Updated passive player after attack:")
  //          println(updatedPassivePlayer)
  //          updatedPassivePlayer
  //        } else {
  //          println(missMessage + s"${passivePlayer.avatar} at coordinates (${passivePlayer.currentPosition.x}, ${passivePlayer.currentPosition.y})!")
  //          passivePlayer // Return the original GameUnit
  //        }
  //
  //      case None =>
  //        println("Invalid target coordinates. Please choose valid coordinates.")
  //        // Return the active player since no target was found
  //        activePlayer
  //    }
  //  }

  def performRangedAttackHttp(avatar: String, targetCoordinates: Coordinates, board: Board): (Board, String) = {
    val potentialTargets = checkRangedAttackHttp(board,avatar)
    val target = potentialTargets.find(_.currentPosition == targetCoordinates)
    val shooter = board.getActiveAlivePlayers.find(_.avatar == avatar)

    (target, shooter) match {
      case (Some(passivePlayer), Some(activePlayer)) =>
        val attackerBS = activePlayer.ballisticSkill
        val randomChance = scala.util.Random.nextInt(100) + 1
        val hitMessage = s"${activePlayer.name} hits ${passivePlayer.name}!"
        val missMessage = s"${activePlayer.name} misses ${passivePlayer.name}!"
        val updatedShooter = activePlayer.copy(shootingPhaseCompleted = true) // Mark shooter as having completed its shooting phase
        println(s"$randomChance vs $attackerBS ")
        if (randomChance <= attackerBS) {
          val updatedPassivePlayer = passivePlayer.copy(state = DEAD_STATE)
          val updatedBoard = board.updatePassiveUnit(updatedPassivePlayer).updateActiveUnit(updatedShooter)
//          println(passivePlayer, activePlayer)
          println("Updated passive player and active player after attack:")
          println(updatedPassivePlayer, updatedShooter)

          // Call checkVictory after ranged attack
          val victoryMessage = new CheckVictoryConditions().checkVictory(updatedBoard.getActivePlayers, updatedBoard.getPassivePlayers)

          (updatedBoard, s"$hitMessage\n${updatedBoard.printBoard()}\n$victoryMessage")
        } else {
          val updatedBoard = board.updateActiveUnit(updatedShooter)
          println("Updated shooter after a miss:")
          println(updatedShooter)

          // Call checkVictory after ranged attack
          val victoryMessage = new CheckVictoryConditions().checkVictory(updatedBoard.getActivePlayers, updatedBoard.getPassivePlayers)

          (updatedBoard, s"$missMessage\n${updatedBoard.printBoard()}\n$victoryMessage")
        }
      case _ =>
        // Call checkVictory after ranged attack
        val victoryMessage = new CheckVictoryConditions().checkVictory(board.getActivePlayers, board.getPassivePlayers)
        (board, s"Invalid target coordinates. Please choose valid coordinates.\n$victoryMessage")
    }
  }


  final def activeUnitsWithNoLineOfSight(activeUnits: List[GameCharacter], map: MapConfig, passiveUnits: List[GameCharacter]): List[GameCharacter] = {
    activeUnits.foreach { unit =>
      println(s"Processing unit: ${unit.avatar}") // Print the current unit being processed
      //      val activePlayerUnits = board.getActivePlayers
      //      val passivePlayerUnits = board.getPassivePlayers
      // Get the current status of passive units, filtering out dead units
      val currentPassiveUnits = passiveUnits.filter(_.state == ALIVE_STATE)

      // Check if the current unit can perform a ranged attack
      val allActiveUnits = activeUnits.filterNot(_ == unit) ++ currentPassiveUnits
      checkRangedAttack(map, unit, currentPassiveUnits, allActiveUnits) match {
        case Nil =>
          // If there are no potential targets, update the shootingPhaseCompleted status of the active player
          val updatedUnit = unit.copy(shootingPhaseCompleted = true)
          println(s"No enemies in range or line of sight. Setting shootingPhaseCompleted for ${updatedUnit.avatar}")

          // Print the unit before and after updating the shootingPhaseCompleted field
          println("Before update:")
          println(unit)
          println("After update:")
          println(updatedUnit)
        case _ => // Do nothing if there are potential targets
      }
    }
    activeUnits // Return the updated list of active units
  }

  def getActiveUnitsAndTargets(board: Board): Map[GameCharacter, List[GameCharacter]] = {
    val mapConfig = board.map
    val activeUnits = board.getActiveAlivePlayers
    val passiveUnits = board.getPassiveAlivePlayers
    val activeUnitsAndTargets = activeUnits.flatMap { unit =>
      val potentialTargets = checkRangedAttack(mapConfig, unit, passiveUnits, activeUnits)
      Map(unit -> potentialTargets)
    }.toMap

    // Print active units before processing
    println("Active Units:")
    activeUnits.foreach(unit => println(unit))
    println("passive Units:")
    passiveUnits.foreach(potentialTarget => println(potentialTarget))
//    val activeUnitsAndTargets = activeUnits.flatMap { unit =>
//      val potentialTargets = checkRangedAttack(mapConfig, unit, passiveUnits, activeUnits)
//      Map(unit -> potentialTargets)
//    }.toMap

    // Print the generated map to the terminal
    println("Active Units and Their Targets:")
    activeUnitsAndTargets.foreach { case (unit, targets) =>
      println(s"${unit.avatar} -> ${targets.map(_.avatar).mkString(", ")}")
    }

    activeUnitsAndTargets
  }


  def httpShoot(coordinates: Coordinates, board: Board, avatar: String): (Board, String) = {

    performRangedAttackHttp(avatar, coordinates, board)

  }


}


