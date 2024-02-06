package jobs

import game.{Coordinates, GameUnit}
import models.{Characters, GameCharacter, MapConfig, Maps}
import models.UnitState.ALIVE_STATE
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
  final def performRangedAttack(mapConfig: MapConfig, activePlayer: GameUnit, passivePlayer: GameUnit, targetCoordinates: Coordinates): GameUnit = {
    //    if (attackPerformed) return passivePlayer

    val targetString: String = s"${targetCoordinates.x} ${targetCoordinates.y}"
    println(s"Would you like to open fire? Enter $targetCoordinates or 'Hold Fire'")
    val input: String = StdIn.readLine()

    if (input.toLowerCase.trim == targetString) {
      val defender = passivePlayer.character.avatar
      val attackerBS = activePlayer.character.ballisticSkill

      val randomChance = Random.nextInt(100) + 1
      println(s"$randomChance vs $attackerBS")

      if (randomChance <= attackerBS) {
        println(activePlayer.character.rangedAttackHitMessage + s"$defender at coordinates $targetCoordinates!")
        // Return a new GameUnit with the updated map configuration and dead state
        passivePlayer.copy(state = "dead", character = passivePlayer.character.copy(avatar = "$"))
      } else {
        println(activePlayer.character.rangedAttackMissMessage + s"$defender at coordinates $targetCoordinates!")
        passivePlayer // Return the original GameUnit
      }
    } else if (input.toLowerCase.trim == "hold fire") {
      passivePlayer
    } else {
      println(s"Invalid input. Please enter $targetCoordinates or 'Hold Fire'.")
      performRangedAttack(mapConfig, activePlayer, passivePlayer, targetCoordinates)
    }

    }

  def checkRangedAttack(mapConfig: MapConfig, activePlayer: GameUnit, passivePlayers: List[GameUnit]): Option[(Coordinates, GameUnit)] = {
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

    val blockerCoordinates = coordinatesAndContents.filter { case (_, content) =>
      content == "X"
    }.map(_._1)

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

    // Filter out cells that are immediately adjacent to the active player's cell
    coordinatesAndContents = coordinatesAndContents.filter { case (c, _) =>
      val (x, y) = (c.x, c.y)
      if (x == activePlayer.coordinates.x && y == activePlayer.coordinates.y + 1) false // Exclude cell to the right
      else if (x == activePlayer.coordinates.x && y == activePlayer.coordinates.y - 1) false // Exclude cell to the left
      else if (y == activePlayer.coordinates.y && x == activePlayer.coordinates.x + 1) false // Exclude cell below
      else if (y == activePlayer.coordinates.y && x == activePlayer.coordinates.x - 1) false // Exclude cell above
      else true
    }

    // Find the first cell that contains a passive player in the ALIVE_STATE
    coordinatesAndContents.find { case (coord, _) =>
      alivePassivePlayers.exists(_.coordinates == coord)
    }.map { case (coord, content) =>
      (coord, alivePassivePlayers.find(_.coordinates == coord).get)
    }
  }


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
@scala.annotation.tailrec
final def performRangedAttackIfInRange(units: List[GameUnit], map: MapConfig, passivePlayers: List[GameUnit]): List[GameUnit] = units match {
  case Nil => units // No more units to process, return the units list
  case unit :: remainingUnits =>
    // Check if the current unit can perform a ranged attack
    checkRangedAttack(map, unit, passivePlayers) match {
      case Some((targetCoordinates, passivePlayer)) =>
        // If the unit can attack, perform the attack and update the units list accordingly
        val updatedUnits = performRangedAttack(map, unit, passivePlayer, targetCoordinates)
        // Continue processing the remaining units with the updated units list
        performRangedAttackIfInRange(remainingUnits, map, passivePlayers)
      case None =>
        // If the unit cannot attack, continue processing the remaining units without any updates
        performRangedAttackIfInRange(remainingUnits, map, passivePlayers)
    }
}





}










