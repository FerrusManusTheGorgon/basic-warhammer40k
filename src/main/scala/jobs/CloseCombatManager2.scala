package jobs

import game.{Coordinates, GameUnit}
import models.{Characters, GameCharacter, MapConfig, Maps}
import models.UnitState.{ALIVE_STATE, DEAD_STATE}

import scala.io.StdIn
import scala.util.Random

class CloseCombatManager2 {


  @scala.annotation.tailrec
  final def performCloseCombatAttack(mapConfig: MapConfig, activePlayer: GameUnit, potentialTargets: List[GameUnit]): GameUnit = {
    // Display the potential targets
    println("Potential targets:")
    potentialTargets.foreach { target =>
      println(s"-${activePlayer.character.avatar} has ${target.character.avatar} at coordinates (${target.coordinates.x}, ${target.coordinates.y})in range and line of sight")
    }

    println("Enter the avatar of the character you want to attack or 'Hold Your Ground'")
    val input: String = StdIn.readLine().toLowerCase.trim

    if (input == "hold your ground") {
      return activePlayer
    }

    // Find the potential target matching the input avatar
    val target = potentialTargets.find(_.character.avatar.toLowerCase == input)

    target match {
      case Some(passivePlayer) =>
        // Randomly determine if the attack hits based on the attacker's ballistic skill
        val attackerWS = activePlayer.character.weaponSkill
        val randomChance = Random.nextInt(100) + 1
        println(s"$randomChance vs $attackerWS")

        val hitMessage = activePlayer.character.closeCombatHitMessage
        val missMessage = activePlayer.character.closeCombatMissMessage
        if (randomChance <= attackerWS) {
          println(hitMessage + s"${passivePlayer.character.avatar} at coordinates (${passivePlayer.coordinates.x}, ${passivePlayer.coordinates.y})!")
          // Update the passive player's state to "dead" and return the updated GameUnit
          val updatedPassivePlayer = passivePlayer.copy(state = DEAD_STATE, character = passivePlayer.character.copy(avatar = "$"))
          println("Updated passive player after attack:")
          println(updatedPassivePlayer)
          updatedPassivePlayer
        } else {
          println(missMessage + s"${passivePlayer.character.avatar} at coordinates (${passivePlayer.coordinates.x}, ${passivePlayer.coordinates.y})!")
          passivePlayer // Return the original GameUnit
        }

      case None =>
        println("Invalid target avatar. Please enter a valid avatar or 'Hold Your Ground'.")
        performCloseCombatAttack(mapConfig, activePlayer, potentialTargets)
    }
  }


  def checkCloseCombatAttack(mapConfig: MapConfig, activePlayer: GameUnit, passivePlayers: List[GameUnit]): List[GameUnit] = {
    println("Checking close combat attack...")

    val row = activePlayer.coordinates.x
    val col = activePlayer.coordinates.y

    // Filter out passive units that are not in the ALIVE_STATE
    val alivePassivePlayers = passivePlayers.filter(_.state == ALIVE_STATE)

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

    val potentialTargets = alivePassivePlayers.filter { p =>
      p.coordinates.x == row || p.coordinates.y == col
    }.map(p => (p.coordinates, p.character.avatar))

    val targetsInSight = alivePassivePlayers.filter { p =>
      coordinatesAndContents.map(_._1).contains(p.coordinates)
    }

    // Print potential targets
    println("Potential targets for close combat:")
    potentialTargets.foreach { target =>
      println(s"- ${target._2} at coordinates (${target._1.x}, ${target._1.y})")
    }

    // Print targets in sight
    println("Targets in sight for close combat:")
    targetsInSight.foreach { target =>
      println(s"- ${target.character.avatar} at coordinates (${target.coordinates.x}, ${target.coordinates.y})")
    }

    targetsInSight
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
  }

  final def performCloseCombatAttackIfInRange(activeUnits: List[GameUnit], map: MapConfig, passiveUnits: List[GameUnit]): List[GameUnit] = {
    activeUnits match {
      case Nil => passiveUnits // No more units to process, return the units list
      case unit :: remainingUnits =>
        println(s"Processing unit: ${unit.character.avatar}") // Print the current unit being processed

        // Get the current status of passive units, filtering out dead units
        val currentPassiveUnits = getCurrentPassiveUnitsStatus(passiveUnits)

        // Check if the current unit can perform a ranged attack
        checkCloseCombatAttack(map, unit, currentPassiveUnits) match {
          case potentialTargets if potentialTargets.nonEmpty =>
            // If there are potential targets, print them
            println("Potential targets for attack:")
            potentialTargets.foreach { target =>
              println(s"- ${target.character.avatar} at coordinates (${target.coordinates.x}, ${target.coordinates.y}), state: ${target.state}")
            }

            // Perform the attack and update the units list accordingly
            val updatedUnit = performCloseCombatAttack(map, unit, potentialTargets)

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
            performCloseCombatAttackIfInRange(remainingUnits, map, remainingPassiveUnits)

          case _ =>
            // If there are no potential targets, print a message and continue processing the remaining units
            println("No enemies in range or line of sight.")
            performCloseCombatAttackIfInRange(remainingUnits, map, currentPassiveUnits)
        }
    }
  }
}







