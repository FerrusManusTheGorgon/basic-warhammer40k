package jobs
//
import game.{Coordinates, GameUnit}
import models.{Characters, MapConfig}

import scala.io.StdIn
import scala.util.Random
import scala.io.StdIn
import scala.util.Random

class CloseCombatManager {

  @scala.annotation.tailrec
  final def performCloseCombatAttack(mapConfig: MapConfig, activePlayer: GameUnit, passivePlayer: GameUnit, targetCoordinates: Coordinates): GameUnit = {
    val targetString: String = s"${targetCoordinates.x} ${targetCoordinates.y}"
    println(s"Would you like to melee Attack? Enter $targetCoordinates or 'Just Stand Your Ground'")
    val input: String = StdIn.readLine()

    if (input.toLowerCase.trim == targetString) {
      val defender = passivePlayer.character.avatar
      val attackerWS = activePlayer.character.weaponSkill

      val randomChance = Random.nextInt(100) + 1
      println(s"$randomChance vs $attackerWS")

      if (randomChance <= attackerWS) {
        println(activePlayer.character.closeCombatHitMessage + s"$defender at coordinates $targetCoordinates!")
        passivePlayer.copy(state = "dead")
      } else {
        println(activePlayer.character.closeCombatMissMessage + s"$defender at coordinates $targetCoordinates!")
        passivePlayer
      }
    } else if (input.toLowerCase.trim == "Stand Your Ground") {
      passivePlayer
    } else {
      println(s"Invalid input. Please enter $targetCoordinates or 'Stand Your Ground'.")
      performCloseCombatAttack(mapConfig, activePlayer, passivePlayer, targetCoordinates)
    }
  }

  def checkCloseCombatAttack(mapConfig: MapConfig, activePlayer: GameUnit, passivePlayer: GameUnit): Option[Coordinates] = {
    println("Checking close combat attack...")
    val row = activePlayer.coordinates.x
    val col = activePlayer.coordinates.y

    // Create a list to store coordinates and their contents
    var coordinatesAndContents: List[(Coordinates, String)] = List.empty

    // Check vertically above
    val coordAbove = Coordinates(row + 1, col)
    val contentAbove = mapConfig.layout.getOrElse(coordAbove, "")
    coordinatesAndContents ::= (coordAbove, contentAbove)

    // Check vertically below
    val coordBelow = Coordinates(row - 1, col)
    val contentBelow = mapConfig.layout.getOrElse(coordBelow, "")
    coordinatesAndContents ::= (coordBelow, contentBelow)

    // Check horizontally left
    val coordLeft = Coordinates(row, col - 1)
    val contentLeft = mapConfig.layout.getOrElse(coordLeft, "")
    coordinatesAndContents ::= (coordLeft, contentLeft)

    // Check horizontally right
    val coordRight = Coordinates(row, col + 1)
    val contentRight = mapConfig.layout.getOrElse(coordRight, "")
    coordinatesAndContents ::= (coordRight, contentRight)

    // Print the filtered list of coordinates and their contents
    println(" List of Coordinates and Contents:")
    coordinatesAndContents.foreach { case (coord, content) =>
      println(s"Coordinate: (${coord.x}, ${coord.y}), Content: $content")
    }

    // Implement your logic here to choose a target for close combat attack
    // For now, let's return the first available coordinate

    coordinatesAndContents.find { case (coord, _) =>
      coord == passivePlayer.coordinates
    }.map(_._1)

  }
  def performCloseCombatAttackIfInRange(mapConfig: MapConfig, activePlayer: GameUnit, passivePlayer: GameUnit): GameUnit = {
    println("Checking close combat attack if in range...")
    checkCloseCombatAttack(mapConfig, activePlayer, passivePlayer) match {
      case Some(targetCoordinates) =>
        performCloseCombatAttack(mapConfig, activePlayer, passivePlayer, targetCoordinates)
      case None =>
        println(s"Unable to assault ${passivePlayer.character.avatar}")
        passivePlayer
    }
  }
}


