package jobs

import game.{Coordinates, GameUnit}
import models.{Characters, GameCharacter, MapConfig, Maps}

class RangeAttackManager2(mapConfig: MapConfig, activePlayerUnit: GameUnit, passivePlayerUnit: GameUnit) {

  val range: Int = activePlayerUnit.character.range

  def checkRangedAttack(position: Coordinates, targetCoordinates: Coordinates): Unit = {
    println("Checking ranged attack...")
    val row = position.x
    val col = position.y

    def isCellBlocked(cell: Coordinates): Boolean = {
      mapConfig.layout.getOrElse(cell, "") == "BLOCKED_SQUARE" //if cell contain block will return true
    }

    def printCellContents(start: Coordinates, end: Coordinates): Unit = {
      // Calculate differences between start and end points
      val dx = end.x - start.x
      val dy = end.y - start.y
      val stepX = if (dx != 0) dx / Math.abs(dx) else 0 //represent the direction of movement along the x and y axes. If dx or dy is zero
      val stepY = if (dy != 0) dy / Math.abs(dy) else 0 // If dx or dy is zero, the corresponding step is set to 0 to avoid division by zero

      var x = start.x
      var y = start.y

      while ((x, y) != (end.x, end.y)) { //loop continues iterating as long as the current coordinates are not equal to the target coordinates
        val currentCell = Coordinates(x, y)
        println(s"Cell ($x, $y): ${mapConfig.layout.getOrElse(currentCell, "")}")
        if (isCellBlocked(currentCell)) {
          println("Path blocked, exiting...")
          return
        }
        x += stepX
        y += stepY
      }

      val finalCell = Coordinates(end.x, end.y)
      println(s"Cell (${finalCell.x}, ${finalCell.y}): ${mapConfig.layout.getOrElse(finalCell, "")}")
      if (isCellBlocked(finalCell)) {
        println("Path blocked, exiting...")
        return
      }

      println("Exiting checkRangedAttack...")
    }

    def printOpponentInRange(coordinates: Coordinates): Unit = {
      val opponent = if (isSpaceMarineMove) "O" else "S"
      println(s"Opponent's character $opponent is in range at coordinates $coordinates")
    }

    // Check vertically above and below within range
    val foundResult: Option[Unit] = (row - range to row + range) //creates a range of x values
      .find(x => x >= 0 && x < mapConfig.verticalLength) //search within the bounds of the map
      .map { x =>
        val currentCell = Coordinates(x, col)
        println(s"Checking cell ($x, $col)...")
        if (isCellBlocked(currentCell)) {
          println("Found blocked cell, exiting...")
        } else if (mapConfig.layout.getOrElse(currentCell, "") == "X") {
          println("Found X, exiting...")
        } else if (mapConfig.layout.getOrElse(currentCell, "") == "O" || mapConfig.layout.getOrElse(currentCell, "") == "S") {
          println(s"Found opponent at ($x, $col), exiting...")
          printOpponentInRange(Coordinates(x, col))
        }
      }

    // If a result is found, execute the block inside Option; otherwise, do nothing
    foundResult.foreach(_ => ())
  }


  // Check horizontally left and right within range
  val foundResult: Option[Unit] = (col - range to col + range)
    .find(y => y >= 0 && y < mapConfig.horizontalLength)
    .map { y =>
      println(s"Checking cell ($row, $y)...")
      if (mapConfig.layout.getOrElse(Coordinates(row, y), "") == "X") {
        println("Found X, exiting...")
      } else if (mapConfig.layout.getOrElse(Coordinates(row, y), "") == "O" || mapConfig.layout.getOrElse(Coordinates(row, y), "") == "S") {
        println(s"Found opponent at ($row, $y), exiting...")
        printOpponentInRange(Coordinates(row, y))
      }
    }

  // If a result is found, execute the block inside Option; otherwise, do nothing
  foundResult.foreach(_ => ())

  def performRangedAttack(targetCoordinates: Coordinates): Unit = {
    val attacker = if (isSpaceMarineMove) "S" else "O"
    val defender = if (isSpaceMarineMove) "O" else "S"
    val attackerBS = if (isSpaceMarineMove) Characters.SpaceMarine.ballisticSkill else Characters.Ork.ballisticSkill

    val randomChance = Random.nextInt(100) + 1

    if (isSpaceMarineMove) {
      if (randomChance <= attackerBS) {
        println(s"Space Marine opened fire with his Bolter and eliminated the $defender at coordinates $targetCoordinates!")
        mapConfig.layout(targetCoordinates) = "" // Remove the defeated enemy from the layout
      } else {
        println(s"Space Marine Bolts missed the $defender at coordinates $targetCoordinates!")
      }
    } else {
      if (randomChance <= attackerBS) {
        println(s"Ork unleashed his Big Shoota and blasted the $attacker to bitz at coordinates $targetCoordinates!")
        mapConfig.layout(targetCoordinates) = "" // Remove the defeated enemy from the layout
      } else {
        println(s"Ork dakka dakka dakka dakka missed $attacker at coordinates $targetCoordinates!")
      }
    }
  }


}





