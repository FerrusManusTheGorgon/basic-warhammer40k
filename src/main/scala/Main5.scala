
import models.Characters

import scala.collection.mutable.ArrayBuffer
import scala.io.StdIn
import scala.util.Random
import scala.collection.mutable.Queue


object Main5 extends App {

  val map: ArrayBuffer[ArrayBuffer[String]] = ArrayBuffer.fill(40)(ArrayBuffer.fill(40)(""))

  var xPos: (Int, Int) = (2, 2)
  var oPos: (Int, Int) = (10, 10)
  val horizontalBlockedCoordinates: List[(Int, Int)] = List((5, 5), (5, 6), (5, 7), (5, 8), (5, 9), (5, 10))
  val verticalBlockedCoordinates: List[(Int, Int)] = List((6, 5), (7, 5), (8, 5), (9, 5), (10, 5), (3, 3), (2, 3), (1, 3))


  var gameStartedX = false
  var gameStartedO = false

  def gameMap(): Unit = {
    for (i <- 0 until 40; j <- 0 until 40)
      map(i)(j) = ""

    // Place X at initial position
    map(xPos._1)(xPos._2) = "X"

    // Place O at initial position
    map(oPos._1)(oPos._2) = "O"

    // Place horizontal blockers
    for ((row, col) <- horizontalBlockedCoordinates) {
      map(row)(col) = "-"
    }

    // Place vertical blockers
    for ((row, col) <- verticalBlockedCoordinates) {
      map(row)(col) = "|"
    }
  }


  def printMap(): Unit = {
    println("+" + ("-" * 40) + "+")
    for (i <- 1 until 40) {
      print("|")
      for (j <- 1 until 40) {
        if (horizontalBlockedCoordinates.contains((i, j))) {
          print("-") // Represents a blocked coordinate
        } else if (verticalBlockedCoordinates.contains((i, j))) {
          print("|") // Represents a blocked coordinate
        } else {
          map(i)(j) match {
            case "X" => print("X")
            case "O" => print("O")
            case _ => print(" ")
          }
        }
      }
      println("|")
    }
    println("+" + ("-" * 40) + "+")
  }


  gameMap()


  var continueGame = true
  var isXMove = true

  while (continueGame) {
    val currentCharacter = if (isXMove) Characters.SpaceMarine else Characters.Ork
    val currentPlayer = if (isXMove) "Player X" else "Player O"

    printMap()

    var validInput = false

    while (!validInput) {
      println(s"$currentPlayer's turn. Current position: ${if (isXMove) "X" else "O"} is at (${if (isXMove) xPos._1 else oPos._1},${if (isXMove) xPos._2 else oPos._2}). Please enter a position (example = 1,1) or 'stay' to remain in the same position")

      var retryInput = false
      val input = StdIn.readLine()
      var x = 0
      var y = 0

      try {
        if (input.toLowerCase() == "stay") {
          checkAttack(oPos)
          checkRangedAttack(oPos)
          printMap()
          validInput = true

          isXMove = !isXMove

        } else if (input.toLowerCase() != "stay") {
          val positions = input.split(",")
          x = positions(0).trim.toInt
          y = positions(1).trim.toInt
        } else {
          if (isXMove) {
            x = xPos._1 + 1 /////// sets the coordinates for possible final destination " + 1" for indexing on the grid map
            y = xPos._2 + 1
          } else {
            x = oPos._1 + 1
            y = oPos._2 + 1
          }
        }

        if (x < 1 || x > 40 || y < 1 || y > 40) { ///// check the make sure the coordinates are on the game map
          println("Position out of range. Please enter coordinates within 1 to 100.")
        } else if ((isXMove && map(x)(y) == "O") || (!isXMove && map(x)(y) == "X")) { ///check if opnoents piece is in the input coordinates
          println("That position is already taken by the opposing player. Please enter a different coordinate.")
        } else {
          val (curX, curY) = if (isXMove) xPos else oPos
          val distance = Math.abs(curX - (x)) + Math.abs(curY - (y)) // calculates distance using manhattan distance measurment

          if (distance > currentCharacter.movement) { /// checks the player characters movement stat will alow the move
            println(s"The distance is greater than ${currentCharacter.movement}. Please enter a valid coordinate.")
          } else {
            // BFS logic for finding shortest path
            val targetCoordinates = (x, y)
            val shortestPath = bfsShortestPath(if (isXMove) xPos else oPos, targetCoordinates, currentCharacter.movement)

            if (shortestPath.isDefined) {
              shortestPath.get.foreach { case (newX, newY) =>
                println(s"New coordinates: ($newX, $newY)")
                if (isXMove) {
                  map(xPos._1)(xPos._2) = ""
                  xPos = (newX, newY)
                  map(xPos._1)(xPos._2) = "X"
                  if (!gameStartedX) gameStartedX = true
                } else {
                  map(oPos._1)(oPos._2) = ""
                  oPos = (newX, newY)
                  map(oPos._1)(oPos._2) = "O"
                  if (!gameStartedO) gameStartedO = true
                }
                // Check for attacks after the move is completed
                if (isXMove) {
                  checkAttack(xPos)
                  checkRangedAttack(xPos)
                } else {
                  checkAttack(oPos)
                  checkRangedAttack(oPos)
                  printMap()
                }
              }

              validInput = true // switches validInput to true to exist the loop

              isXMove = !isXMove // switches next player

              if (gameStartedX && gameStartedO && (!map.flatten.contains("X") || !map.flatten.contains("O"))) { ///gameStartedX && gameStartedO needed so game is not declared after player 1 goes first. Then check if Player X or O is missing from the board
                if (!map.flatten.contains("X")) {
                  println("The Ork wins! The Hummie was Krumped.")
                } else {
                  println("The Space Marine wins! The Xeno scum has been purged.")
                }
                continueGame = false // ends the game
              }
            } else {
              println("No valid path within movement limit. Please enter a new coordinate.")
              retryInput = true // begins the process of the loop
              x = 0
              y = 0
            }
          }
        }
        validInput = !retryInput //loops back through the movement process
      } catch {
        case _: Throwable =>
          println("Invalid input, please try again") /// catch statement to handle other possible inputs
      }
    }
  }


  def bfsShortestPath(start: (Int, Int), end: (Int, Int), maxMovement: Int): Option[List[(Int, Int)]] = {
    val moves = List((0, 1), (0, -1), (1, 0), (-1, 0)) //possible moves (up, down, left, right) in the grid

    val visited = Array.fill(40, 40)(false) // 2D array to keep track of visited coordinates, sets all values to false
    val path = Array.fill(40, 40)((-1, -1)) // 2D array to keep track of the path taken. nitializes a 2D array of size 40x40 where each element is a tuple (-1, -1)

    val queue = Queue[(Int, Int)]() // mutable collection based on FIFO
    val steps = Array.fill(40, 40)(0) // Track path length instead of steps in the queue

    queue.enqueue(start) //add an element to the end of the queue
    visited(start._1)(start._2) = true

    while (queue.nonEmpty) {
      val current = queue.dequeue() //remove and retrieve the element at the front of the queue

      if (current == end) { //urrent position being examined in the breadth-first search equals the target destination
        val shortestPath = scala.collection.mutable.ListBuffer[(Int, Int)]()
        var currentPos = end
        var pathLength = 0 // Initialize path length counter

        while (currentPos != start) {
          shortestPath.prepend(currentPos) //The currentPos is added to the beginning of the shortestPath list. This operation adds the current position to the front of the list, forming the shortest path from the end to the start position.
          pathLength += 1 // Increment path length for each step
          currentPos = path(currentPos._1)(currentPos._2) // t updates the currentPos to the position stored in the path array at the coordinates specified by the current currentPos (currentPos._1 and currentPos._2). This step moves backward in the path from the current position to its predecessor along the shortest path.
        }
        ///The loop continues until the currentPos reaches the start position, effectively reconstructing the shortest path from the end to the start position by backtracking through the path array.

        shortestPath.prepend(start) // adds the start position to the beginning of the shortestPath list.
        // path is constructed in reverse order during the backtracking process.
        // (start) is the last element added to the shortestPath list when tracing back the path.
        if (pathLength <= maxMovement) {
          // Print the shortest path coordinates if within movement limit
          println("Shortest Path Coordinates:")
          shortestPath.foreach(println)

          return Some(shortestPath.toList) //converts the ListBuffer shortestPath to a regular immutable List
        }
      }

      for ((dx, dy) <- moves) { ///performs a series of checks to explore potential movements in different directions from the current position
        val newX = current._1 + dx
        val newY = current._2 + dy

        if (newX >= 0 && newX < 40 && newY >= 0 && newY < 40 && // checks boundaries
          !visited(newX)(newY) && //avoids revisiting already explored positions, preventing infinite loop
          map(newX)(newY) != "X" && map(newX)(newY) != "O" && // avoids enemy pieces and blocking cells
          map(newX)(newY) != "-" && map(newX)(newY) != "|" &&
          !horizontalBlockedCoordinates.contains((newX, newY)) && // duplicated blocking cells
          !verticalBlockedCoordinates.contains((newX, newY)) &&
          steps(current._1)(current._2) + 1 <= maxMovement) { // Check path length
          queue.enqueue((newX, newY)) ///adds it as visited
          visited(newX)(newY) = true //marks it as visited
          path(newX)(newY) = current // updates current position
          steps(newX)(newY) = steps(current._1)(current._2) + 1 // Update path length
        }
      }
    }

    None // If no path found within movement limit
  }


  def checkAttack(position: (Int, Int)): Unit = {
    // Define adjacent positions (up, down, left, right) relative to the given position
    val adjacentPositions = List(
      (position._1 - 1, position._2), (position._1 + 1, position._2),
      (position._1, position._2 - 1), (position._1, position._2 + 1)
    )

    val attacker = if (isXMove) "X" else "O"
    val defender = if (isXMove) "O" else "X"
    val attackerWS = if (isXMove) Characters.SpaceMarine.weaponSkill else Characters.Ork.weaponSkill


    adjacentPositions.foreach { case (x, y) =>
      if (x >= 0 && x < 40 && y >= 0 && y < 40 && map(x)(y) == defender) {
        val randomChance = Random.nextInt(100) + 1
        if (randomChance <= attackerWS) {
          println(s"$attacker attacked $defender and slaughtered!")
          map(x)(y) = ""
        } else {
          println(s"$attacker swung at $defender but missed!")
        }
      }
    }
  }

  def checkRangedAttack(position: (Int, Int)): Unit = {
    val row = position._1
    val col = position._2
    val currentCharacter = if (isXMove) Characters.SpaceMarine else Characters.Ork
    val range = currentCharacter.range

    def printCellContents(start: (Int, Int), end: (Int, Int)): Unit = {
      // Calculate differences between start and end points
      val dx = end._1 - start._1
      val dy = end._2 - start._2
      val stepX = if (dx != 0) dx / Math.abs(dx) else 0
      val stepY = if (dy != 0) dy / Math.abs(dy) else 0

      var x = start._1
      var y = start._2

      while ((x, y) != (end._1, end._2)) {
        println(s"Cell ($x, $y): ${map(x)(y)}")
        x += stepX
        y += stepY
      }
      println(s"Cell (${end._1}, ${end._2}): ${map(end._1)(end._2)}")
    }

    // Check vertically above and below within range
    // Look for targets (O) or obstacles (- or |) in horizontal lines of sight
    for (x <- row - 1 to Math.max(0, row - range) by -1) {
      if (map(x)(col) == "|" || map(x)(col) == "-") {
        return
      } else if (map(x)(col) == "O") {
        printCellContents(position, (x, col))
        performRangedAttack((x, col))
        return
      } else if (map(x)(col) == "X") {
        return
      }
    }

    for (x <- row + 1 to Math.min(99, row + range)) {
      if (map(x)(col) == "|" || map(x)(col) == "-") {
        return
      } else if (map(x)(col) == "O") {
        printCellContents(position, (x, col))
        performRangedAttack((x, col))
        return
      } else if (map(x)(col) == "X") {
        return
      }
    }

    // Check horizontally left and right within range
    for (y <- col - 1 to Math.max(0, col - range) by -1) {
      if (map(row)(y) == "|" || map(row)(y) == "-") {
        return
      } else if (map(row)(y) == "O") {
        printCellContents(position, (row, y))
        performRangedAttack((row, y))
        return
      } else if (map(row)(y) == "X") {
        return
      }
    }

    for (y <- col + 1 to Math.min(99, col + range)) {
      if (map(row)(y) == "|" || map(row)(y) == "-") {
        return
      } else if (map(row)(y) == "O") {
        printCellContents(position, (row, y))
        performRangedAttack((row, y))
        return
      } else if (map(row)(y) == "X") {
        return
      }
    }
  }


  def performRangedAttack(position: (Int, Int)): Unit = {
    val attacker = if (isXMove) "X" else "O"
    val defender = if (isXMove) "O" else "X"
    val attackerBS = if (isXMove) Characters.SpaceMarine.ballisticSkill else Characters.Ork.ballisticSkill

    val randomChance = Random.nextInt(100) + 1

    if (isXMove) {
      if (randomChance <= attackerBS) {
        println("Space Marine opened fire with his Bolter and eliminated the Ork!")
        map(position._1)(position._2) = "" // Remove the defeated enemy
      } else {
        println("Space Marine Bolts missed the Ork !")
      }
    } else {
      if (randomChance <= attackerBS) {
        println("Ork unleashed his Big Shoota and blasted the Space Marine to bitz!")
        map(position._1)(position._2) = "" // Remove the defeated enemy
      } else {
        println("Ork dakka dakka dakka dakka missed!")
      }
    }
  }


}
