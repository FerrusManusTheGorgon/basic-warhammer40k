package jobs

import models.Characters

import scala.io.StdIn
import scala.util.Random

class RangeAttackManager(isXMove: Boolean, gameMap: GameMap) {

  private val currentCharacter = if (isXMove) Characters.SpaceMarine else Characters.Ork
  private val range = currentCharacter.range

  def checkRangedAttack(position: (Int, Int)): Unit = {
    println("Checking ranged attack...")
    val row = position._1
    val col = position._2

    def printCellContents(start: (Int, Int), end: (Int, Int)): Unit = {
      // Calculate differences between start and end points
      val dx = end._1 - start._1
      val dy = end._2 - start._2
      val stepX = if (dx != 0) dx / Math.abs(dx) else 0
      val stepY = if (dy != 0) dy / Math.abs(dy) else 0

      var x = start._1
      var y = start._2

      while ((x, y) != (end._1, end._2)) {
        println(s"Cell ($x, $y): ${gameMap.map(x)(y)}")
        x += stepX
        y += stepY
      }
      println(s"Cell (${end._1}, ${end._2}): ${gameMap.map(end._1)(end._2)}")
      println("Exiting checkRangedAttack...")
    }

    def printOpponentInRange(coordinates: (Int, Int)): Unit = {
      val opponent = if (isXMove) "O" else "S"
      println(s"Opponent's character $opponent is in range at coordinates $coordinates")
    }

    // Check vertically above and below within range
    for (x <- row - range to row + range) {
      if (x >= 0 && x < gameMap.verticalLength) {
        println(s"Checking cell ($x, $col)...")
        if (gameMap.map(x)(col) == "X") {
          println("Found X, exiting...")
          return
        } else if (gameMap.map(x)(col) == "O" || gameMap.map(x)(col) == "S") {
          println(s"Found opponent at ($x, $col), exiting...")
          printOpponentInRange((x, col))
          return
        }
      }
    }

    // Check horizontally left and right within range
    for (y <- col - range to col + range) {
      if (y >= 0 && y < gameMap.horizontalLength) {
        println(s"Checking cell ($row, $y)...")
        if (gameMap.map(row)(y) == "X") {
          println("Found X, exiting...")
          return
        } else if (gameMap.map(row)(y) == "O" || gameMap.map(row)(y) == "S") {
          println(s"Found opponent at ($row, $y), exiting...")
          printOpponentInRange((row, y))
          return
        }
      }
    }

    println("No opponent in range.")
  }
//  def checkRangedAttack(position: (Int, Int)): Unit = {
//    println("Checking ranged attack...")
//    val row = position._1
//    val col = position._2
//    val currentCharacter = if (isXMove) Characters.SpaceMarine else Characters.Ork//new
//    val range = currentCharacter.range //new
//
//    def printCellContents(start: (Int, Int), end: (Int, Int)): Unit = {
//      // Calculate differences between start and end points
//      val dx = end._1 - start._1
//      val dy = end._2 - start._2
//      val stepX = if (dx != 0) dx / Math.abs(dx) else 0
//      val stepY = if (dy != 0) dy / Math.abs(dy) else 0
//
//      var x = start._1
//      var y = start._2
//
//      while ((x, y) != (end._1, end._2)) {
//        println(s"Cell ($x, $y): ${gameMap.map(x)(y)}")
//        x += stepX
//        y += stepY
//      }
////      println(s"Cell (${end._1}, ${end._2}): ${gameMap.map(end._1)(end._2)}") //old
//
//      println("Exiting checkRangedAttack...")
//    }
//
//    def printOpponentInRange(coordinates: (Int, Int)): Unit = {
//      val opponent = if (isXMove) "O" else "S"
//      println(s"Opponent's character $opponent is in range at coordinates $coordinates")
//    }
//
//    // Check vertically above and below within range
//    for (x <- row - range to row + range) {
//      if (x >= 0 && x < gameMap.verticalLength) {
//        println(s"Checking cell ($x, $col)...")
//        if (gameMap.map(x)(col) == "X") {
//          println("Found X, exiting...")
//          return
//        } else if (gameMap.map(x)(col) == "O" || gameMap.map(x)(col) == "S") {
//          println(s"Found opponent at ($x, $col), exiting...")
//          printOpponentInRange((x, col))
//          return
//        }
//      }
//    }
//    for (x <- row - 1 to Math.max(0, row - range) by -1) {
//      if (map(x)(col) == "|" || map(x)(col) == "-") {
//        return
//      } else if (map(x)(col) == "O") {
//        printCellContents(position, (x, col))
////        performRangedAttack((x, col))
//        return
//      } else if (map(x)(col) == "X") {
//        return
//      }
//    }
//
//    for (x <- row + 1 to Math.min(99, row + range)) {
//      if (map(x)(col) == "|" || map(x)(col) == "-") {
//        return
//      } else if (map(x)(col) == "O") {
//        printCellContents(position, (x, col))
////        performRangedAttack((x, col))
//        return
//      } else if (map(x)(col) == "X") {
//        return
//      }
//    }
//
//    // Check horizontally left and right within range
//    for (y <- col - 1 to Math.max(0, col - range) by -1) {
//      if (map(row)(y) == "|" || map(row)(y) == "-") {
//        return
//      } else if (map(row)(y) == "O") {
//        printCellContents(position, (row, y))
////        performRangedAttack((row, y))
//        return
//      } else if (map(row)(y) == "X") {
//        return
//      }
//    }
//
//    for (y <- col + 1 to Math.min(99, col + range)) {
//      if (map(row)(y) == "|" || map(row)(y) == "-") {
//        return
//      } else if (map(row)(y) == "O") {
//        printCellContents(position, (row, y))
////        performRangedAttack((row, y))
//        return
//      } else if (map(row)(y) == "X") {
//        return
//      }
//    }
//  }
    // Check horizontally left and right within range
//    for (y <- col - range to col + range) { //old
//      if (y >= 0 && y < gameMap.horizontalLength) {
//        if (gameMap.map(row)(y) == "X") {
//          return
//        } else if (gameMap.map(row)(y) == "O" || gameMap.map(row)(y) == "S") {
//          printOpponentInRange((row, y))
//          return
//        }
//      }
//    }



  def performRangedAttack(position: (Int, Int), targetCoordinates: (Int, Int)): Unit = {
    val attacker = if (isXMove) "S" else "O"
    val defender = if (isXMove) "O" else "S"
    val attackerBS = if (isXMove) Characters.SpaceMarine.ballisticSkill else Characters.Ork.ballisticSkill

    val randomChance = Random.nextInt(100) + 1

    println("Perform range attack (enter target's coordinates) or hold your fire:")
    val input = StdIn.readLine()

    if (input.toLowerCase() != "hold fire") {
      try {
        val coordinates = input.split(",").map(_.trim.toInt)
        val positionInGameMap = gameMap.convertCoordinates(position) // Convert player's position
        val targetCoordinatesInGameMap = gameMap.convertCoordinates(targetCoordinates) // Convert target's position

        if (coordinates.length == 2 && (coordinates(0), coordinates(1)) == targetCoordinatesInGameMap) {
          if (isInRange(positionInGameMap, targetCoordinatesInGameMap)) {
            if (randomChance <= attackerBS) {
              if (isXMove) {
                println("Space Marine opened fire with his Bolter and eliminated the Ork!")
              } else {
                println("Ork unleashed his Big Shoota and blasted the Space Marine to bits!")
              }
              gameMap.map(targetCoordinatesInGameMap._1)(targetCoordinatesInGameMap._2) = "" // Remove the defeated enemy
            } else {
              if (isXMove) {
                println("Space Marine Bolts missed the Ork!")
              } else {
                println("Ork dakka dakka dakka dakka missed!")
              }
            }
          } else {
            println("Target is out of range.")
          }
        } else {
          println("Input coordinates do not match the opponent's coordinates in range.")
        }
      } catch {
        case _: Throwable =>
          println("Invalid input for coordinates. Please enter in the format 'x,y'.")
      }
    } else {
      println("Hold fire command received. Skipping ranged attack.")
    }
  }

  def isInRange(position: (Int, Int), targetCoordinates: (Int, Int)): Boolean = {
    // Define your logic to check if the target is in range
    // For example:
    val distanceX = Math.abs(position._1 - targetCoordinates._1)
    val distanceY = Math.abs(position._2 - targetCoordinates._2)
    distanceX <= range && distanceY <= range
  }
}

