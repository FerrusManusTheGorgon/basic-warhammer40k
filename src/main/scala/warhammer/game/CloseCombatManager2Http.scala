package warhammer.game

import warhammer.game.models.UnitState.{ALIVE_STATE, DEAD_STATE}
import warhammer.game.models.{Board, Coordinates, GameCharacter, MapConfig}
import scalacache.modes.sync.mode
import scalacache.{Cache, sync}

import scala.io.StdIn
import scala.util.Random


class CloseCombatManager2Http (implicit cache: Cache[Board]) {


  @scala.annotation.tailrec
  final def performCloseCombatAttack(mapConfig: MapConfig, activePlayer: GameCharacter, potentialTargets: List[GameCharacter]): GameCharacter = {
    // Display the potential targets
    println("Potential targets:")
    potentialTargets.foreach { target =>
      println(s"-${activePlayer.avatar} ${activePlayer.name} has ${target.avatar} ${target.name} at coordinates (${target.currentPosition.x}, ${target.currentPosition.y})in assault range")
    }

    println("Enter the avatar of the character you want to attack or 'Hold Your Ground'")
    val input: String = StdIn.readLine().toLowerCase.trim

    if (input == "hold your ground") {
      return activePlayer
    }

    // Find the potential target matching the input avatar
    val target = potentialTargets.find(_.avatar.toLowerCase == input)

    target match {
      case Some(passivePlayer) =>
        // Randomly determine if the attack hits based on the attacker's ballistic skill
        val attackerWS = activePlayer.weaponSkill
        val randomChance = Random.nextInt(100) + 1
        println(s"$randomChance vs $attackerWS")

        val hitMessage = activePlayer.closeCombatHitMessage
        val missMessage = activePlayer.closeCombatMissMessage

        if (randomChance <= attackerWS) {
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
        println("Invalid target avatar. Please enter a valid avatar or 'Hold Your Ground'.")
        performCloseCombatAttack(mapConfig, activePlayer, potentialTargets)
    }
  }

  //  def meleeMessage(prefix: String, passivePlayer: GameUnit): Unit = {
  //    println(prefix + s"${passivePlayer.character.avatar} at coordinates (${passivePlayer.coordinates.x}, ${passivePlayer.coordinates.y})!")
  //  }

  def checkCloseCombatAttack(mapConfig: MapConfig, activePlayer: GameCharacter, passivePlayers: List[GameCharacter]): List[GameCharacter] = {
    println("Checking close combat attack...")
    println(passivePlayers, activePlayer)
    val potentialMeleeTargets = passivePlayers.filter(p => activePlayer.currentPosition.adjacent.contains(p.currentPosition))

    // Print targets in sight
    println("Targets within striking distance for close combat:")
    potentialMeleeTargets.foreach { target =>
      println(s"- ${target.avatar} at coordinates (${target.currentPosition.x}, ${target.currentPosition.y})")
    }

    potentialMeleeTargets
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

  final def performCloseCombatAttackIfInRange(activeUnits: List[GameCharacter], map: MapConfig, passiveUnits: List[GameCharacter]): List[GameCharacter] = {
    activeUnits match {
      case Nil => passiveUnits // No more units to process, return the units list
      case unit :: remainingUnits =>
        println(s"Processing unit: ${unit.avatar}") // Print the current unit being processed

        // Get the current status of passive units, filtering out dead units
        val currentPassiveUnits = getCurrentPassiveUnitsStatus(passiveUnits)

        // Check if the current unit can perform a ranged attack
        checkCloseCombatAttack(map, unit, currentPassiveUnits) match {
          case potentialTargets if potentialTargets.nonEmpty =>
            // If there are potential targets, print them
            println("Potential targets for attack:")
            potentialTargets.foreach { target =>
              println(s"- ${target.avatar} at coordinates (${target.currentPosition.x}, ${target.currentPosition.y}), state: ${target.state}")
            }

            // Perform the attack and update the units list accordingly
            val potentialCasualty = performCloseCombatAttack(map, unit, potentialTargets)

            val remainingPassiveUnits = if (potentialCasualty.state == ALIVE_STATE) {
              currentPassiveUnits
            } else {
              currentPassiveUnits.filterNot(p => p.characterId == potentialCasualty.characterId)
            }

            // Print the current status of passive units after filtering
            println("Current passive units status:")
            remainingPassiveUnits.foreach { unit =>
              println(s"- ${unit.avatar} at coordinates (${unit.currentPosition.x}, ${unit.currentPosition.y}), state: ${unit.state}")
            }

            // Continue processing the remaining units with the updated units list
            performCloseCombatAttackIfInRange(remainingUnits, map, remainingPassiveUnits)
          case _ =>
            // If there are no potential targets, print a message and continue processing the remaining units
            println("No enemies within striking distance.")
            performCloseCombatAttackIfInRange(remainingUnits, map, currentPassiveUnits)
        }
    }
  }


  def performCloseCombatAttackHttpIfInRange(assaultCoordinates: Coordinates, boardId: String, avatar: String): String = {

    val cachedBoard: Option[Board] = sync.get(boardId)
    cachedBoard match {
      case Some(board) =>
        // Get the active player's units from the board
        val activePlayerUnits = board.getActivePlayers
        val currentAssaultUnitOption = activePlayerUnits.find(p => p.avatar == avatar && !p.closeCombatPhaseCompleted)
        currentAssaultUnitOption match {
          case Some(currentAssaultUnit) =>
            if (assaultCoordinates == Coordinates(100, 100)) {
              // Special case for coordinates (100, 100)
              val updatedUnit = currentAssaultUnit.copy(closeCombatPhaseCompleted = true)
              val updatedBoard = board.updateActiveUnit(updatedUnit)
              sync.put(boardId)(updatedBoard)
              updatedBoard.printBoard()
              s"$avatar held its attack"
            } else {
              // Check ranged attack for the current shoot unit
              checkCloseCombatAttack(board.map, currentAssaultUnit, board.getPassivePlayers) match {
                case Nil =>
                  s"No enemies in assault range  for ${currentAssaultUnit.avatar} at coordinates: $assaultCoordinates"
                case targets =>
                  // If there are potential targets, print them
                  println("Potential targets for assault:")
                  targets.foreach { target =>
                    println(s"- ${target.avatar} at coordinates (${target.currentPosition.x}, ${target.currentPosition.y}), state: ${target.state}")
                    val updatedTarget = target.copy(avatar = " ", state = "dead")
                    board.updateActiveUnit(updatedTarget)
                  }

                  val updatedAssaultUnit = currentAssaultUnit.copy(closeCombatPhaseCompleted = true)
                  // Update the board with the modified targets
                  val updatedBoard = board.updateActiveUnit(updatedAssaultUnit)
                  sync.put(boardId)(updatedBoard)
                  updatedBoard.printBoard()
                  "Close Combat attack performed successfully."
              }
            }
          case None =>
            "No units available for Close Combat attack."
        }
      case None =>
        // If the board with the provided boardId is not found in the cache, return an error message
        "Board not found. Please provide a valid boardId."
    }
  }

  def checkCloseCombatAttackHttp(board: Board): List[GameCharacter] = {
    val currentAssaultUnitOption = board.getActivePlayers.find(p => !p.closeCombatPhaseCompleted && p.state == "alive").get
    checkCloseCombatAttack(board.map, currentAssaultUnitOption, board.getPassivePlayers)
    //
    //
  }


  def performCloseCombatAttackHttp(mapConfig: MapConfig, activePlayer: GameCharacter, targetCoordinates: Coordinates, potentialTargets: List[GameCharacter], board: Board): (Board, String) = {
    val target = potentialTargets.find(_.currentPosition == targetCoordinates)
    target match {
      case Some(passivePlayer) =>
        val attackerWS = activePlayer.weaponSkill
        val randomChance = scala.util.Random.nextInt(100) + 1
        val hitMessage = s"${activePlayer.name} hits ${passivePlayer.name}!"
        val missMessage = s"${activePlayer.name} misses ${passivePlayer.name}!"
        val updatedAssaulter = activePlayer.copy(closeCombatPhaseCompleted = true) // Mark asaulter as having completed its shooting phase
        if (randomChance <= attackerWS) {
          val updatedPassivePlayer = passivePlayer.copy(state = DEAD_STATE, avatar = "")
          val updatedBoard = board.updatePassiveUnit(updatedPassivePlayer).updateActiveUnit(updatedAssaulter)

          println("Updated passive player and active player after attack:")
          println(updatedPassivePlayer, updatedAssaulter)
          (updatedBoard, s"$hitMessage\n${updatedBoard.printBoard()}")
        } else {
          val updatedBoard = board.updateActiveUnit(updatedAssaulter)
          println("Updated shooter after a miss:")
          println(updatedAssaulter)
          (updatedBoard, s"$missMessage\n${updatedBoard.printBoard()}")
        }
      case None =>
        (board, "Invalid target coordinates. Please choose valid coordinates.")
    }
  }

  final def activeUnitsNotInAssaultRange(activeUnits: List[GameCharacter], map: MapConfig, passiveUnits: List[GameCharacter]): List[GameCharacter] = {
    activeUnits.flatMap { unit =>
      println(s"Processing unit: ${unit.avatar}") // Print the current unit being processed

      // Get the current status of passive units, filtering out dead units
      val currentPassiveUnits = passiveUnits.filter(_.state == ALIVE_STATE)

      // Check if the current unit can perform a close combat attack
      val potentialTargets = checkCloseCombatAttack(map, unit, currentPassiveUnits)

      if (potentialTargets.isEmpty) {
        // If there are no potential targets, update the closeCombatPhaseCompleted status of the active player
        val updatedUnit = unit.copy(closeCombatPhaseCompleted = true)
        println(s"No enemies in assault range. Setting closeCombatPhaseCompleted for ${updatedUnit.avatar}")

        // Print the unit before and after updating the closeCombatPhaseCompleted field
        println("Before update:")
        println(unit)
        println("After update:")
        println(updatedUnit)

        Some(updatedUnit)
      } else {
        None // Return None if there are potential targets
      }
    }
  }

  def getActiveUnitsAndAssaultTargets(board: Board): Map[GameCharacter, List[GameCharacter]] = {
    val mapConfig = board.map
    val activeUnits = board.getActivePlayers
    val passiveUnits = board.getPassivePlayers
    val activeUnitsAndTargets = activeUnits.flatMap { unit =>
      val potentialTargets = checkCloseCombatAttack(mapConfig, unit, passiveUnits)
      Map(unit -> potentialTargets)
    }.toMap

    // Print the generated map to the terminal
    println("Active Units and Their AssaultTargets:")
    val filteredActiveUnitsAndTargets = activeUnitsAndTargets.filter { case (unit, _) =>
      unit.state == "alive"
    }
    filteredActiveUnitsAndTargets.foreach { case (unit, targets) =>
      println(s"${unit.avatar} -> ${targets.map(_.avatar).mkString(", ")}")
    }

    filteredActiveUnitsAndTargets


  }
}










