package jobs

import models.Characters

import scala.io.StdIn
import scala.util.Random
import scala.io.StdIn
import scala.util.Random

class CloseCombatManager(mapConfig: MapConfig, activePlayerUnit: GameUnit, passivePlayerUnit: GameUnit) {

  def initiateCloseCombatAttack(): Unit = {
    val validEnemyCoordinates = getValidEnemyCoordinates()
    println(s"Valid enemy coordinates: $validEnemyCoordinates")

    println("Please enter coordinates of the enemy you would like to attack (format: x y): ")
    val input: String = StdIn.readLine()
    val attackCoordinates: Coordinates = parseCoordinates(input)

    if (isValidAttackCoordinates(attackCoordinates)) {
      checkCloseCombatAttack(attackCoordinates)
    } else {
      println("Invalid coordinates. The attack cannot be initiated.")
    }
  }

  private def getValidEnemyCoordinates(): String = {
    val validCoordinates = mapConfig.layout.collect {
      case (coordinates, symbol) if symbol == getDefenderSymbol() => s"(${coordinates.x}, ${coordinates.y})"
    }.mkString(", ")

    if (validCoordinates.nonEmpty) validCoordinates else "No valid enemy coordinates available."
  }

  private def isValidAttackCoordinates(coordinates: Coordinates): Boolean = {
    mapConfig.isWithinBounds(coordinates) &&
      mapConfig.layout.getOrElse(coordinates, "") == getDefenderSymbol()
  }

  private def getDefenderSymbol(): String = {
    if (isSpaceMarineMove) "O" else "S"
  }

  private def checkCloseCombatAttack(attackCoordinates: Coordinates): Unit = {
    val attacker = if (isSpaceMarineMove) "S" else "O"
    val defender = if (isSpaceMarineMove) "O" else "S"
    val attackerBS = if (isSpaceMarineMove) Characters.SpaceMarine.ballisticSkill else Characters.Ork.ballisticSkill

    val randomChance = Random.nextInt(100) + 1
    if (randomChance <= attackerBS) {
      println(s"$attacker attacked $defender and eliminated the opponent at coordinates (${attackCoordinates.x}, ${attackCoordinates.y})!")
      mapConfig.layout(attackCoordinates) = "" // Remove the defeated enemy from the layout
    } else {
      println(s"$attacker's attack missed $defender at coordinates (${attackCoordinates.x}, ${attackCoordinates.y})!")
    }
  }

  private def parseCoordinates(input: String): Coordinates = {
    val Array(x, y) = input.split("\\s+").map(_.toInt)
    Coordinates(x, y)
  }
}

