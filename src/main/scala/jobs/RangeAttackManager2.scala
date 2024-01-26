package jobs

import game.{Coordinates, GameUnit}
import models.{Characters, GameCharacter, MapConfig, Maps}
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
  def performRangedAttack(mapConfig: MapConfig, activePlayer: GameUnit, passivePlayer: GameUnit): GameUnit = {
    val defender = passivePlayer.character.avatar
    val attackerBS = activePlayer.character.ballisticSkill

    val randomChance = Random.nextInt(100) + 1
    println(s"$randomChance vs $attackerBS")

    if (randomChance <= attackerBS) {
      println(activePlayer.character.rangedAttackHitMessage + s"$defender at coordinates ${passivePlayer.coordinates}!")
      // Return a new GameUnit with the updated map configuration and dead state
      passivePlayer.copy(state = "dead")
    } else {
      println(activePlayer.character.rangedAttackMissMessage + s"$defender at coordinates ${passivePlayer.coordinates}!")
      passivePlayer // Return the original GameUnit
    }

  }


  // Method to check ranged attack
  def checkRangedAttack(mapConfig: MapConfig, activePlayer: GameUnit, passivePlayer: GameUnit): Unit = {
    println("Checking ranged attack...")
    val row = activePlayer.coordinates.x
    val col = activePlayer.coordinates.y
    val range = activePlayer.character.range
    val opponent = passivePlayer.character.avatar
    // Check vertically above and below within range
    (row - range to row + range)
      .find(x => x >= 0 && x < mapConfig.verticalLength)
      .foreach { x =>
        val currentCell = Coordinates(x, col)
        println(s"Checking cell ($x, $col)...")
        if (isCellBlocked(mapConfig, currentCell)) {
          println("Found blocked cell, exiting...")
        } else if (mapConfig.layout.getOrElse(currentCell, "") == "X") {
          println("Found X, exiting...")
        } else if (mapConfig.layout.getOrElse(currentCell, "") == "O" || mapConfig.layout.getOrElse(currentCell, "") == "S") {
          println(s"Found opponent at ($x, $col), exiting...")
          printOpponentInRange(opponent, Coordinates(x, col))
        }
      }

    // Check horizontally left and right within range
    (col - range to col + range)
      .find(y => y >= 0 && y < mapConfig.horizontalLength)
      .foreach { y =>
        println(s"Checking cell ($row, $y)...")
        if (mapConfig.layout.getOrElse(Coordinates(row, y), "") == "X") {
          println("Found X, exiting...")
        } else if (mapConfig.layout.getOrElse(Coordinates(row, y), "") == "O" || mapConfig.layout.getOrElse(Coordinates(row, y), "") == "S") {
          println(s"Found opponent at ($row, $y), exiting...")
          printOpponentInRange(opponent, Coordinates(row, y))
        }
      }
  }
}






