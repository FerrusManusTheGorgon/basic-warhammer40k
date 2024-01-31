package jobs

import game.{Coordinates, GameUnit}
import models.MapConfig

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

  // Method to perform ranged attack
  // Method to perform ranged attack
  @scala.annotation.tailrec
  final def performRangedAttack(mapConfig: MapConfig, activePlayer: GameUnit, passivePlayer: GameUnit, targetCoordinates: Option[Coordinates], attackPerformed: Boolean = false): GameUnit = {
    if (attackPerformed) return passivePlayer

    println(s"Would you like to open fire? Enter $targetCoordinates or 'Hold Fire'")
    val input: String = StdIn.readLine()

    input.toLowerCase match {
      case s"$targetCoordinates"  =>
        val defender = passivePlayer.character.avatar
        val attackerBS = activePlayer.character.ballisticSkill

        val randomChance = Random.nextInt(100) + 1
        println(s"$randomChance vs $attackerBS")

        if (randomChance <= attackerBS) {
          println(activePlayer.character.rangedAttackHitMessage + s"$defender at coordinates $targetCoordinates!")
          // Return a new GameUnit with the updated map configuration and dead state
          passivePlayer.copy(state = "dead")
        } else {
          println(activePlayer.character.rangedAttackMissMessage + s"$defender at coordinates $targetCoordinates!")
          passivePlayer // Return the original GameUnit
        }
      case "hold fire" =>
        passivePlayer
      case _ =>
        println(s"Invalid input. Please enter $targetCoordinates or 'Hold Fire'.")
        performRangedAttack(mapConfig, activePlayer, passivePlayer, targetCoordinates, attackPerformed = false)
    }
  }



  def checkRangedAttack(mapConfig: MapConfig, activePlayer: GameUnit, passivePlayer: GameUnit): Option[Coordinates] = {
    println("Checking ranged attack...")
    val effectiveRange = math.min(activePlayer.character.range, math.max(mapConfig.horizontalLength, mapConfig.verticalLength))
    val opponent = passivePlayer.character.avatar
    val row = activePlayer.coordinates.x
    val col = activePlayer.coordinates.y

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

    // Find the coordinate of the blocked cell, if any
    val blockerCoordinates = coordinatesAndContents.find { case (_, content) =>
      content == "X"
    }.map(_._1)

    // Filter out coordinates and contents based on the position of the blocked cell
    val filteredCoordinates = blockerCoordinates match {
      case Some(blockedCoord: Coordinates) => // Ensure blockedCoord is of type Coordinates
        val (bx, by) = (blockedCoord.x.toInt, blockedCoord.y.toInt)
        coordinatesAndContents.filter { case (c, _) =>
          val (x, y) = (c.x.toInt, c.y.toInt)
          if (x == bx && y == by) false // Exclude the blocked cell itself
          else if (x == row && y == col) true // Keep the current cell
          else if (x == row && y > col && by > col) false // Exclude cells to the right of the blocked cell
          else if (x == row && y < col && by < col) false // Exclude cells to the left of the blocked cell
          else if (y == col && x > row && bx > row) false // Exclude cells above the blocked cell
          else if (y == col && x < row && bx < row) false // Exclude cells below the blocked cell
          else true // Keep cells in other directions
        }
      case None => coordinatesAndContents // No blocked cell found, return the original list
    }


    // Filter out coordinates and contents based on the position of the passive player
    val filteredCoordinatesPassive = filteredCoordinates.filter { case (coord, _) =>
      coord == passivePlayer.coordinates
    }

    // Replace contents with passive player's avatar if the coordinate matches passive player's current coordinates
    val updatedCoordinatesAndContents = filteredCoordinatesPassive.map { case (coord, content) =>
      (coord, passivePlayer.character.avatar)
    }

    if (updatedCoordinatesAndContents.nonEmpty) {
      updatedCoordinatesAndContents.foreach { case (coord, content) =>
        println(s"Coordinate: (${coord.x}, ${coord.y}), Content: $content")
      }
      // Return the coordinates of the passive player
      Some(passivePlayer.coordinates)
    } else {
      println("No passive player in range.")
      None
    }
  }


  // Method to perform ranged attack only if the passive player is in range
  def performRangedAttackIfInRange(mapConfig: MapConfig, activePlayer: GameUnit, passivePlayer: GameUnit): Unit = {
    checkRangedAttack(mapConfig, activePlayer, passivePlayer) match {
      case Some(targetCoordinates) =>
        performRangedAttack(mapConfig, activePlayer, passivePlayer, Some(targetCoordinates))
      case None =>
        println(s"Unable to open fire on the ${passivePlayer.character.range}")
    }
  }
}


//class RangeAttackManager2 {
//
//
//  // Method to check if a cell is blocked
//  private def isCellBlocked(mapConfig: MapConfig, cell: Coordinates): Boolean = {
//    mapConfig.layout.getOrElse(cell, "") == "BLOCKED_SQUARE"
//  }
//
//  // Method to print opponent in range
//  private def printOpponentInRange(opponent: String, coordinates: Coordinates): Unit = {
//    println(s"Opponent's character $opponent is in range at coordinates $coordinates")
//  }
//
//  // Method to perform ranged attack
//  def performRangedAttack(mapConfig: MapConfig, activePlayer: GameUnit, passivePlayer: GameUnit, targetCoordinates: Option[Coordinates]): GameUnit = {
//    targetCoordinates match {
//      case Some(coordinates) =>
//        val defender = passivePlayer.character.avatar
//        val attackerBS = activePlayer.character.ballisticSkill
//
//        val randomChance = Random.nextInt(100) + 1
//        println(s"$randomChance vs $attackerBS")
//
//        if (randomChance <= attackerBS) {
//          println(activePlayer.character.rangedAttackHitMessage + s"$defender at coordinates $coordinates!")
//          // Return a new GameUnit with the updated map configuration and dead state
//          passivePlayer.copy(state = "dead")
//        } else {
//          println(activePlayer.character.rangedAttackMissMessage + s"$defender at coordinates $coordinates!")
//          passivePlayer // Return the original GameUnit
//        }
//
//      case None =>
//        println("No passive player in range.")
//        activePlayer // Return the original active player
//    }
//  }
//
//
//  // Method to check ranged attack
//  //  def checkRangedAttack(mapConfig: MapConfig, activePlayer: GameUnit, passivePlayer: GameUnit): Unit = {
//  //    println("Checking ranged attack...")
//  //    val row = activePlayer.coordinates.x
//  //    val col = activePlayer.coordinates.y
//  //    val range = activePlayer.character.range
//  //    val opponent = passivePlayer.character.avatar
//
//  // Method to check ranged attack
//  def checkRangedAttack(mapConfig: MapConfig, activePlayer: GameUnit, passivePlayer: GameUnit): Option[Coordinates] = {
//    println("Checking ranged attack...")
//    val effectiveRange = math.min(activePlayer.character.range, math.max(mapConfig.horizontalLength, mapConfig.verticalLength))
//    val opponent = passivePlayer.character.avatar
//    val row = activePlayer.coordinates.x
//    val col = activePlayer.coordinates.y
//
//    // Create a list to store coordinates and their contents
//    var coordinatesAndContents: List[(Coordinates, String)] = List.empty
//
//    // Check vertically above within effective range
//    for (dx <- 1 to effectiveRange) {
//      val rowAbove = row + dx
//      if (rowAbove < mapConfig.verticalLength + 1) {
//        val coord = Coordinates(rowAbove, col)
//        val content = mapConfig.layout.getOrElse(coord, "")
//        coordinatesAndContents ::= (coord, content)
//      }
//    }
//
//    // Check vertically below within effective range
//    for (dx <- 1 to effectiveRange) {
//      val rowBelow = row - dx
//      if (rowBelow >= 0) {
//        val coord = Coordinates(rowBelow, col)
//        val content = mapConfig.layout.getOrElse(coord, "")
//        coordinatesAndContents ::= (coord, content)
//      }
//    }
//
//    // Check horizontally left within effective range
//    for (dy <- 1 to effectiveRange) {
//      val colLeft = col - dy
//      if (colLeft >= 0) {
//        val coord = Coordinates(row, colLeft)
//        val content = mapConfig.layout.getOrElse(coord, "")
//        coordinatesAndContents ::= (coord, content)
//      }
//    }
//
//    // Check horizontally right within effective range
//    for (dy <- 1 to effectiveRange) {
//      val colRight = col + dy
//      if (colRight < mapConfig.horizontalLength + 1) {
//        val coord = Coordinates(row, colRight)
//        val content = mapConfig.layout.getOrElse(coord, "")
//        coordinatesAndContents ::= (coord, content) // current cell and its content to the beginning of the list
//      }
//    }
//
//    // Find the coordinate of the blocked cell, if any
//    //    val blockedCoordinate = coordinatesAndContents.find { case (_, content) =>
//    //      content == "X"
//    //    }.map(_._1) // Extracting only the coordinate from the found tuple
//    val blockerCoordinates = coordinatesAndContents.filter { case (_, content) =>
//      content == "X"
//    }.map(_._1)
//    //val blockedCoordinates = mapConfig.blockers
//    // Filter out coordinates and contents based on the position of the blocked cell
//    blockerCoordinates.foreach { blockedCoordinate =>
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
//
//      }
//    }
//
//
//    coordinatesAndContents.foreach { case (coord, content) =>
//      println(s"Coordinate: (${coord.x}, ${coord.y}), Content: $content")
//    }
//
//    // Replace contents with passive player's avatar if the coordinate matches passive player's current coordinates
//    coordinatesAndContents = coordinatesAndContents.map { case (coord, content) =>
//      if (coord == passivePlayer.coordinates) (coord, passivePlayer.character.avatar)
//      else (coord, content)
//    }
//
//    // Print the updated list of coordinates and their contents
//    println("Updated List of Coordinates and Contents:")
//    coordinatesAndContents.foreach { case (coord, content) =>
//      println(s"Coordinate: (${coord.x}, ${coord.y}), Content: $content")
//    }

//    coordinatesAndContents.find { case (_, content) =>
//      content == passivePlayer.character.avatar
//    }.map(_._1)


// get the x coordinates
// get the y coordinates
// filter out things that are out of bounds
// filter out things not in range
//check blockers up
//checker blocker down
//check blocker right
// check blocker left


//    // Check vertically above and below within range
//    (row - range to row + range)
//      .find(x => x >= 0 && x < mapConfig.verticalLength)
//      .foreach { x =>
//        val currentCell = Coordinates(x, col)
//        println(s"Checking cell ($x, $col)...")
//        if (isCellBlocked(mapConfig, currentCell)) {
//          println("Found blocked cell, exiting...")
//        } else if (mapConfig.layout.getOrElse(currentCell, "") == "X") {
//          println("Found X, exiting...")
//        } else if (mapConfig.layout.getOrElse(currentCell, "") == "O" || mapConfig.layout.getOrElse(currentCell, "") == "S") {
//          println(s"Found opponent at ($x, $col), exiting...")
//          printOpponentInRange(opponent, Coordinates(x, col))
//        }
//      }
//
//    // Check horizontally left and right within range
//    (col - range to col + range)
//      .find(y => y >= 0 && y < mapConfig.horizontalLength)
//      .foreach { y =>
//        println(s"Checking cell ($row, $y)...")
//        if (mapConfig.layout.getOrElse(Coordinates(row, y), "") == "X") {
//          println("Found X, exiting...")
//        } else if (mapConfig.layout.getOrElse(Coordinates(row, y), "") == "O" || mapConfig.layout.getOrElse(Coordinates(row, y), "") == "S") {
//          println(s"Found opponent at ($row, $y), exiting...")
//          printOpponentInRange(opponent, Coordinates(row, y))
//        }
//      }









