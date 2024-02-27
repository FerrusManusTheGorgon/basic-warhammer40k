package jobs


  import game.GameUnit
  import models.{Characters, Coordinates, GameCharacter, MapConfig, Maps}
  import models.UnitState.{ALIVE_STATE, DEAD_STATE}

  import scala.io.StdIn
  import scala.util.Random


  class RangeAttackMangerHttp {


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
        println(s"-${activePlayer.character.avatar} ${activePlayer.character.name} has ${target.character.avatar} ${target.character.name} at coordinates (${target.coordinates.x}, ${target.coordinates.y})in range and line of sight")
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


    def checkRangedAttack(mapConfig: MapConfig, activePlayer: GameUnit, passivePlayers: List[GameUnit], allActiveUnits: List[GameUnit]): List[GameUnit] = {
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



      val blockerCoordinates = coordinatesAndContents.flatMap { case (coord, _) =>
        if (mapConfig.blocker.contains(coord) || allActiveUnits.exists(_.coordinates == coord)) {
          println(s"Blocked coordinate at (${coord.x}, ${coord.y})")
          Some(coord)
        } else None
      }


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
    }


    // Method to perform ranged attacks if passive units are in range

    final def performRangedAttackIfInRange(activeUnits: List[GameUnit], finishedUnits: List[GameUnit], map: MapConfig, passiveUnits: List[GameUnit]): List[GameUnit] = {
      activeUnits match {
        case Nil => passiveUnits // No more units to process, return the units list
        case unit :: remainingUnits =>
          println(s"Processing unit: ${unit.character.avatar}") // Print the current unit being processed

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
              performRangedAttackIfInRange(remainingUnits, finishedUnits ++ List(unit), map, remainingPassiveUnits)


            case _ =>
              // If there are no potential targets, print a message and continue processing the remaining units
              println("No enemies in range or line of sight.")
              performRangedAttackIfInRange(remainingUnits, finishedUnits ++ List(unit), map, currentPassiveUnits)
          }
      }
    }


  }


