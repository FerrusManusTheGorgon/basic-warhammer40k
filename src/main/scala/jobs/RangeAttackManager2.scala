package jobs

import game.{Coordinates, GameUnit}
import models.{Characters, GameCharacter, MapConfig, Maps}
import models.UnitState.{ALIVE_STATE, DEAD_STATE}

import scala.io.StdIn
import scala.util.Random


class RangeAttackManager2 {


  // Method to check if a cell is blocked
  private def isCellBlocked(mapConfig: MapConfig, cell: Coordinates): Boolean = {
    mapConfig.layout.getOrElse(cell, "") == "BLOCKED_SQUARE"
  }

  // Method to print opponent in range
  private def printOpponentInRange(opponent: String, coordinates: Coordinates): Unit = {
    println(s"Opponent's character $opponent is in range at coordinates $coordinates")
  }

  @scala.annotation.tailrec
  final def performRangedAttack(mapConfig: MapConfig, activePlayer: GameUnit, potentialTargets: List[GameUnit]): GameUnit = {
    // Display the potential targets
    println("Potential targets:")
    potentialTargets.foreach { target =>
      println(s"-${activePlayer.character.avatar} has ${target.character.avatar} at coordinates (${target.coordinates.x}, ${target.coordinates.y})in range and line of sight")
    }

    println("Enter the avatar of the character you want to attack or 'Hold Fire'")
    val input: String = StdIn.readLine().toLowerCase.trim

    if (input == "hold fire") {
      return activePlayer
    }

    // Find the potential target matching the input avatar
    val target = potentialTargets.find(_.character.avatar.toLowerCase == input)

    target match {
      case Some(passivePlayer) =>
        // Randomly determine if the attack hits based on the attacker's ballistic skill
        val attackerBS = activePlayer.character.ballisticSkill
        val randomChance = Random.nextInt(100) + 1
        println(s"$randomChance vs $attackerBS")

        val hitMessage = activePlayer.character.rangedAttackHitMessage
        val missMessage = activePlayer.character.rangedAttackMissMessage

        if (randomChance <= attackerBS) {
          println(hitMessage + s"${passivePlayer.character.avatar} at coordinates (${passivePlayer.coordinates.x}, ${passivePlayer.coordinates.y})!")
          // Update the passive player's state to "dead" and return the updated GameUnit
          val updatedPassivePlayer = passivePlayer.copy(state = DEAD_STATE, character = passivePlayer.character.copy(avatar = ""))
          println("Updated passive player after attack:")
          println(updatedPassivePlayer)
          updatedPassivePlayer
        } else {
          println(missMessage + s"${passivePlayer.character.avatar} at coordinates (${passivePlayer.coordinates.x}, ${passivePlayer.coordinates.y})!")
          passivePlayer // Return the original GameUnit
        }

      case None =>
        println("Invalid target avatar. Please enter a valid avatar or 'Hold Fire'.")
        performRangedAttack(mapConfig, activePlayer, potentialTargets)
    }
  }


  //  @scala.annotation.tailrec
//  final def performRangedAttack(mapConfig: MapConfig, activePlayer: GameUnit, passiveUnits: List[GameUnit]): GameUnit = {
//    //    if (attackPerformed) return passivePlayer
//
//    val targetString: String = s"${targetCoordinates.x} ${targetCoordinates.y}"
//    println(s"Would you like to open fire? Enter $targetCoordinates or 'Hold Fire'")
//    val input: String = StdIn.readLine()
//
//    if (input.toLowerCase.trim == targetString) {
//      val defender = passivePlayer.character.avatar
//      val attackerBS = activePlayer.character.ballisticSkill
//
//      val randomChance = Random.nextInt(100) + 1
//      println(s"$randomChance vs $attackerBS")
//
//      if (randomChance <= attackerBS) {
//        println(activePlayer.character.rangedAttackHitMessage + s"$defender at coordinates $targetCoordinates!")
//        // Return a new GameUnit with the updated map configuration and dead state
//        passivePlayer.copy(state = "dead", character = passivePlayer.character.copy(avatar = "$"))
//      } else {
//        println(activePlayer.character.rangedAttackMissMessage + s"$defender at coordinates $targetCoordinates!")
//        passivePlayer // Return the original GameUnit
//      }
//    } else if (input.toLowerCase.trim == "hold fire") {
//      passivePlayer
//    } else {
//      println(s"Invalid input. Please enter $targetCoordinates or 'Hold Fire'.")
//      performRangedAttack(mapConfig, activePlayer, passivePlayer, targetCoordinates)
//    }
//
//  }

  //top
  def checkRangedAttack(mapConfig: MapConfig, activePlayer: GameUnit, passivePlayers: List[GameUnit]): List[GameUnit] = {
    println("Checking ranged attack...")
    val effectiveRange = math.min(activePlayer.character.range, math.max(mapConfig.horizontalLength, mapConfig.verticalLength))

    val row = activePlayer.coordinates.x
    val col = activePlayer.coordinates.y

    // Filter out passive units that are not in the ALIVE_STATE
    val alivePassivePlayers = passivePlayers.filter(_.state == ALIVE_STATE)

    // Create a list to store coordinates and their contents
    var coordinatesAndContents: List[(Coordinates, String)] = List.empty

    // Check vertically above within effective range
    for (dx <- 1 to effectiveRange) {
      val rowAbove = row + dx
      if (rowAbove < mapConfig.verticalLength + 1) {
        val coord = Coordinates(rowAbove, col)
        val content = mapConfig.layout.getOrElse(coord, "")
        coordinatesAndContents ::= (coord, content)
      }
    }

    // Check vertically below within effective range
    for (dx <- 1 to effectiveRange) {
      val rowBelow = row - dx
      if (rowBelow >= 0) {
        val coord = Coordinates(rowBelow, col)
        val content = mapConfig.layout.getOrElse(coord, "")
        coordinatesAndContents ::= (coord, content)
      }
    }

    // Check horizontally left within effective range
    for (dy <- 1 to effectiveRange) {
      val colLeft = col - dy
      if (colLeft >= 0) {
        val coord = Coordinates(row, colLeft)
        val content = mapConfig.layout.getOrElse(coord, "")
        coordinatesAndContents ::= (coord, content)
      }
    }

    // Check horizontally right within effective range
    for (dy <- 1 to effectiveRange) {
      val colRight = col + dy
      if (colRight < mapConfig.horizontalLength + 1) {
        val coord = Coordinates(row, colRight)
        val content = mapConfig.layout.getOrElse(coord, "")
        coordinatesAndContents ::= (coord, content) // current cell and its content to the beginning of the list
      }
    }

    val potentialTargets = alivePassivePlayers.filter { p =>
      p.coordinates.x == row || p.coordinates.y == col
    }.map(p => (p.coordinates, p.character.avatar))

    val blockerCoordinates = coordinatesAndContents.filter { case (_, content) =>
      content == "X"
    }.map(_._1)
    //    val blockerCoordinates = coordinatesAndContents.filter { case (_, content) =>//TODO match character coords to cell list
    //      content != "" && content != "$"
    //    }.map(_._1)

//    coordinatesAndContents = coordinatesAndContents ++ potentialTargets
//
//    potentialTargets.map(_._1) ++ blockerCoordinates.foreach { blockedCoordinate =>
//      val (bx, by) = (blockedCoordinate.x, blockedCoordinate.y)
//      coordinatesAndContents = coordinatesAndContents.filter { case (c, _) =>
//        val (x, y) = (c.x, c.y)
//        if (x == bx && y == by) false // Exclude the blocked cell itself
//        else if (x == row && y == col) true // Keep the current cell
//        else if (x == row && y > col && by > col) false // Exclude cells to the right of the blocked cell
//        else if (x == row && y < col && by < col) false // Exclude cells to the left of the blocked cell
//        else if (y == col && x > row && bx > row) false // Exclude cells above the blocked cell
//        else if (y == col && x < row && bx < row) false // Exclude cells below the blocked cell
//        else true // Keep cells in other directions
//      }
//    }
val targetCoordinates = potentialTargets.map(_._1)
    blockerCoordinates.foreach { blockedCoordinate =>
      val (bx, by) = (blockedCoordinate.x, blockedCoordinate.y)
      coordinatesAndContents = coordinatesAndContents.filter { case (c, _) =>
        val (x, y) = (c.x, c.y)
        if (x == bx && y == by) false // Exclude the blocked cell itself
        else if (x == row && y == col) true // Keep the current cell
        else if (x == row && y > col && by > col) false // Exclude cells to the right of the blocked cell
        else if (x == row && y < col && by < col) false // Exclude cells to the left of the blocked cell
        else if (y == col && x > row && bx > row) false // Exclude cells above the blocked cell
        else if (y == col && x < row && bx < row) false // Exclude cells below the blocked cell
        else true // Keep cells in other directions
      }
    }

    targetCoordinates ++ coordinatesAndContents.map(_._1)


    // Filter out cells that are immediately adjacent to the active player's cell
    coordinatesAndContents = coordinatesAndContents.filter { case (c, _) => //TODO use adjacent from coordinates
      val (x, y) = (c.x, c.y)
      if (x == activePlayer.coordinates.x && y == activePlayer.coordinates.y + 1) false // Exclude cell to the right
      else if (x == activePlayer.coordinates.x && y == activePlayer.coordinates.y - 1) false // Exclude cell to the left
      else if (y == activePlayer.coordinates.y && x == activePlayer.coordinates.x + 1) false // Exclude cell below
      else if (y == activePlayer.coordinates.y && x == activePlayer.coordinates.x - 1) false // Exclude cell above
      else true
    }

    // Find the first cell that contains a passive player in the ALIVE_STATE USE A FILTER HERE
    //    coordinatesAndContents.find { case (coord, _) =>
    //      alivePassivePlayers.exists(_.coordinates == coord)
    //    }.map { case (coord, content) =>
    //      (coord, alivePassivePlayers.find(_.coordinates == coord).get)
    //    }
    //    coordinatesAndContents.filter{ case (coord, _) =>
    //      alivePassivePlayers.exists(_.coordinates == coord)
    //    }
    alivePassivePlayers.filter { p =>
      coordinatesAndContents.map(_._1).contains(p.coordinates)
    }
  }

  // Method to get the current status of passive units, filtering out dead units
  def getCurrentPassiveUnitsStatus(passiveUnits: List[GameUnit]): List[GameUnit] = {
    // Filter out units that are not in the DEAD_STATE
    val alivePassiveUnits = passiveUnits.filter(_.state == ALIVE_STATE)
    println("Current passive units status:")
    alivePassiveUnits.foreach { unit =>
      println(s"- ${unit.character.avatar} at coordinates (${unit.coordinates.x}, ${unit.coordinates.y}), state: ${unit.state}")
    }
    alivePassiveUnits
  } // bottom
  //def checkRangedAttack(mapConfig: MapConfig, activePlayer: GameUnit, passivePlayers: List[GameUnit]): List[(Coordinates, GameUnit)] = {
  //  println("Checking ranged attack...")
  //  val effectiveRange = math.min(activePlayer.character.range, math.max(mapConfig.horizontalLength, mapConfig.verticalLength))
  //
  //  val row = activePlayer.coordinates.x
  //  val col = activePlayer.coordinates.y
  //
  //  // Filter out passive units that are not in the ALIVE_STATE
  //  val alivePassivePlayers = passivePlayers.filter(_.state == ALIVE_STATE)
  //
  //  // Create a list to store coordinates and their contents as a val
  //  val coordinatesAndContents: List[(Coordinates, String)] = {
  //    // Define the function to check cells within the effective range
  //    def checkCellsInRange(dx: Int, dy: Int): List[(Coordinates, String)] = {
  //      (1 to effectiveRange).flatMap { distance =>
  //        val newRow = row + dx * distance
  //        val newCol = col + dy * distance
  //        if (newRow >= 0 && newRow < mapConfig.verticalLength && newCol >= 0 && newCol < mapConfig.horizontalLength) {
  //          val coord = Coordinates(newRow, newCol)
  //          val content = mapConfig.layout.getOrElse(coord, "")
  //          Some((coord, content))
  //        } else {
  //          None
  //        }
  //      }.toList
  //    }
  //
  //    // Get the cells within effective range in all directions
  //    val cellsAbove = checkCellsInRange(1, 0)
  //    val cellsBelow = checkCellsInRange(-1, 0)
  //    val cellsLeft = checkCellsInRange(0, -1)
  //    val cellsRight = checkCellsInRange(0, 1)
  //
  //    cellsAbove ++ cellsBelow ++ cellsLeft ++ cellsRight
  //  }
  //
  //  // Filter out blocked cells
  //  val blockerCoordinates = coordinatesAndContents.filter { case (_, content) =>
  //    content != "" && content != "$"
  //  }.map(_._1)
  //
  //  val updatedCoordinatesAndContents = blockerCoordinates.foldLeft(coordinatesAndContents) { (acc, blockedCoordinate) =>
  //    acc.filter { case (c, _) =>
  //      val (x, y) = (c.x, c.y)
  //      val (bx, by) = (blockedCoordinate.x, blockedCoordinate.y)
  //      (x != bx || y != by) && (x != row || y != col) &&
  //        !(x == row && y > col && by > col) &&
  //        !(x == row && y < col && by < col) &&
  //        !(y == col && x > row && bx > row) &&
  //        !(y == col && x < row && bx < row)
  //    }
  //  }
  //
  //  // Filter out cells that are immediately adjacent to the active player's cell
  //  val filteredCoordinatesAndContents = updatedCoordinatesAndContents.filter { case (c, _) =>
  //    val (x, y) = (c.x, c.y)
  //    (x != activePlayer.coordinates.x || y != activePlayer.coordinates.y + 1) &&
  //      (x != activePlayer.coordinates.x || y != activePlayer.coordinates.y - 1) &&
  //      (y != activePlayer.coordinates.y || x != activePlayer.coordinates.x + 1) &&
  //      (y != activePlayer.coordinates.y || x != activePlayer.coordinates.x - 1)
  //  }
  //
  //  // Filter out cells that contain a passive player in the ALIVE_STATE
  //  val filteredPassivePlayerCells = filteredCoordinatesAndContents.filter { case (coord, _) =>
  //    alivePassivePlayers.exists(_.coordinates == coord)
  //  }
  //
  //  // Map each filtered cell to return the cell and the corresponding passive player
  //  filteredPassivePlayerCells.map { case (coord, _) =>
  //    (coord, alivePassivePlayers.find(_.coordinates == coord).get)
  //  }
  //}


  //  // Method to perform ranged attack only if the passive player is in range
  //  def performRangedAttackIfInRange(mapConfig: MapConfig, activePlayer: GameUnit, passivePlayer: GameUnit): GameUnit = {
  //    checkRangedAttack(mapConfig, activePlayer, passivePlayer) match {
  //      case Some(targetCoordinates) =>
  //        performRangedAttack(mapConfig, activePlayer, passivePlayer, targetCoordinates)
  //      case None =>
  //        println(s"Unable to open fire on the ${passivePlayer.character.avatar}")
  //        passivePlayer
  //    }
  //  }

  //top
  //  @scala.annotation.tailrec
  //  final def performRangedAttackIfInRange(activeUnits: List[GameUnit], map: MapConfig, passiveUnits: List[GameUnit]): List[GameUnit] = {
  //    activeUnits match {
  //      case Nil => passiveUnits // No more units to process, return the units list
  //      case unit :: remainingUnits =>
  //        // Check if the current unit can perform a ranged attack
  //        checkRangedAttack(map, unit, passiveUnits) match {
  //          case potentialTargets => // TODO FIX
  //            // If the unit can attack, perform the attack and update the units list accordingly
  //            val updatedUnit = performRangedAttack(map, unit, potentialTargets)
  //            // Continue processing the remaining units with the updated units list
  //            // TODO update passiveUnits with updatedUnits pass in instead of updatedUnits 258
  //            performRangedAttackIfInRange(remainingUnits, map, updatedUnits)
  //          case Nil =>
  //            // If the unit cannot attack, continue processing the remaining units without any updates
  //            performRangedAttackIfInRange(remainingUnits, map, passiveUnits)
  //        }
  //      }
  //bottom
   // Method to perform ranged attacks if passive units are in range

  // Method to perform ranged attacks if passive units are in range
  final def performRangedAttackIfInRange(activeUnits: List[GameUnit], map: MapConfig, passiveUnits: List[GameUnit]): List[GameUnit] = {
    activeUnits match {
      case Nil => passiveUnits // No more units to process, return the units list
      case unit :: remainingUnits =>
        println(s"Processing unit: ${unit.character.avatar}") // Print the current unit being processed

        // Get the current status of passive units, filtering out dead units
        val currentPassiveUnits = getCurrentPassiveUnitsStatus(passiveUnits)

        // Check if the current unit can perform a ranged attack
        checkRangedAttack(map, unit, currentPassiveUnits) match {
          case potentialTargets if potentialTargets.nonEmpty =>
            // If there are potential targets, print them
            println("Potential targets for attack:")
            potentialTargets.foreach { target =>
              println(s"- ${target.character.avatar} at coordinates (${target.coordinates.x}, ${target.coordinates.y}), state: ${target.state}")
            }

            // Perform the attack and update the units list accordingly
            val updatedUnit = performRangedAttack(map, unit, potentialTargets)

            // Filter out passive units that were eliminated during the attack
            val remainingPassiveUnits = currentPassiveUnits.map { passiveUnit =>
              if (passiveUnit.coordinates == updatedUnit.coordinates) updatedUnit else passiveUnit
            }.filter(_.state == ALIVE_STATE)


            // Print the current status of passive units after filtering
            println("Current passive units status:")
            remainingPassiveUnits.foreach { unit =>
              println(s"- ${unit.character.avatar} at coordinates (${unit.coordinates.x}, ${unit.coordinates.y}), state: ${unit.state}")
            }

            // Print the remaining passive units after filtering
            println("Remaining passive units after attack:")
            remainingPassiveUnits.foreach { unit =>
              println(s"- ${unit.character.avatar} at coordinates (${unit.coordinates.x}, ${unit.coordinates.y}), state: ${unit.state}")
            }

            // Continue processing the remaining units with the updated units list
            performRangedAttackIfInRange(remainingUnits, map, remainingPassiveUnits)

          case _ =>
            // If there are no potential targets, print a message and continue processing the remaining units
            println("No enemies in range or line of sight.")
            performRangedAttackIfInRange(remainingUnits, map, currentPassiveUnits)
        }
    }
  }











  //  @scala.annotation.tailrec
    //  final def performRangedAttackIfInRange(activeUnits: List[GameUnit], map: MapConfig, passivePlayers: List[GameUnit]): List[GameUnit] = activeUnits match {
    //    case Nil => passivePlayers // No more units to process, return the units list
    //    case unit :: remainingUnits =>
    //      // Check if the current unit can perform a ranged attack
    //      checkRangedAttack(map, unit, passivePlayers) match {
    //        case Nil =>
    //          // If no passive players are in range, continue processing the remaining units without any updates
    //          performRangedAttackIfInRange(remainingUnits, map, passivePlayers)
    //        case targets =>
    //          // If there are passive players in range, perform the attack for each target and update the units list accordingly
    //          // If there are passive players in range, perform the attack for each target and update the units list accordingly
    //          val updatedUnits = targets.foldLeft(passivePlayers) { case (acc, (targetCoordinates, targetPassivePlayer)) =>
    //            performRangedAttack(map, unit, acc.find(_.coordinates == targetCoordinates).get, targetCoordinates)
    //          }
    //
    //
    //      }
    //          // Continue processing the remaining units with the updated units list
    //          performRangedAttackIfInRange(remainingUnits, map, updatedUnits)
    //      }
  }















