package jobs

import scala.collection.mutable.Queue
import scala.io.StdIn
import models.{GameCharacter, Characters, MapConfig, Maps}
import models.Maps._

class MovementImpl(map: MapConfig) {
  var xPos: (Int, Int) = (1, 2)
  var oPos: (Int, Int) = (4, 5)

  def playerMove(): Unit = {
    val currentCharacter = Characters.SpaceMarine
    val currentPlayer = "Player X"

    var validInput = false

    while (!validInput) {
      println(s"$currentPlayer's turn. Current position: ${if (currentPlayer == "Player X") "X" else "O"} is at (${if (currentPlayer == "Player X") xPos._1 else oPos._1},${if (currentPlayer == "Player X") xPos._2 else oPos._2}). Please enter a position (example = 1,1) or 'stay' to remain in the same position")

      var retryInput = false
      val input = StdIn.readLine()
      var x = 0
      var y = 0

      try {
        if (input.toLowerCase() == "stay") {
          // Logic for 'stay'
          x = if (currentPlayer == "Player X") xPos._1 else oPos._1
          y = if (currentPlayer == "Player X") xPos._2 else oPos._2
          validInput = true
          retryInput = false // Ensure retryInput is set to false to exit the loop
        } else if (input.toLowerCase() != "stay") {
          val positions = input.split(",")
          x = positions(0).trim.toInt
          y = positions(1).trim.toInt
        } else {
          // Adjust coordinates for the possible final destination
          x = if (currentPlayer == "Player X") xPos._1 + 1 else oPos._1 + 1
          y = if (currentPlayer == "Player X") xPos._2 + 1 else oPos._2 + 1
        }

        if (x < 1 || x > map.horizontalLength || y < 1 || y > map.verticalLength) {
          println(s"Position out of range. Please enter coordinates within 1 to ${map.horizontalLength} horizontally and 1 to ${map.verticalLength} vertically.")
          retryInput = true
        } else if (map.blocker.contains(s"$x,$y") || isOccupiedByOtherCharacter(x, y, currentPlayer)) {
          // The position is blocked or occupied by another character
          println("That position is either blocked or occupied by another character. Please enter a different coordinate.")
          retryInput = true // Set retryInput to true to loop back through
        } else {
          validInput = true
        }
      } catch {
        case _: Throwable =>
          println("Invalid input, please try again")
      }

      validInput = !retryInput
    }
  }

  private def isOccupiedByOtherCharacter(x: Int, y: Int, currentPlayer: String): Boolean = {
    if (currentPlayer == "Player X") {
      oPos == (x, y)
    } else {
      xPos == (x, y)
    }
  }

  def bfsShortestPath(start: (Int, Int), end: (Int, Int), maxMovement: Int): Option[List[(Int, Int)]] = {
    val moves = List((0, 1), (0, -1), (1, 0), (-1, 0))

    val visited = Array.fill(40, 40)(false)
    val path = Array.fill(40, 40)((-1, -1))
    val queue = Queue[(Int, Int)]()
    val steps = Array.fill(40, 40)(0)

    queue.enqueue(start)
    visited(start._1)(start._2) = true

    while (queue.nonEmpty) {
      val current = queue.dequeue()

      println(s"Exploring cell: $current")

      if (current == end) {
        val shortestPath = scala.collection.mutable.ListBuffer[(Int, Int)]()
        var currentPos = end
        var pathLength = 0

        while (currentPos != start) {
          shortestPath.prepend(currentPos)
          pathLength += 1
          currentPos = path(currentPos._1)(currentPos._2)
        }

        shortestPath.prepend(start)
        println(s"Path length: $pathLength, Max movement: $maxMovement")

        if (pathLength <= maxMovement) {
          println("Shortest Path Coordinates:")
          shortestPath.foreach(println)
          return Some(shortestPath.toList)
        }
      }

      for ((dx, dy) <- moves) {
        val newX = current._1 + dx
        val newY = current._2 + dy

        if (newX >= 1 && newX < 40 && newY >= 1 && newY < 40 &&
          !visited(newX)(newY) &&
          !map.blocker.contains(s"$newX,$newY") &&
          steps(current._1)(current._2) + 1 <= maxMovement) {

          println(s"Enqueuing cell: ($newX, $newY)")
          queue.enqueue((newX, newY))
          visited(newX)(newY) = true
          path(newX)(newY) = current
          steps(newX)(newY) = steps(current._1)(current._2) + 1
        }
      }
    }

    None
  }
}

//class MovementImpl {
//
//  //val isXMove = True
//  def getPlayerMove(currentPlayer: String, isXMove: Boolean): (Int, Int) = {
//    println(s"$currentPlayer's turn. Current position: ${if (isXMove) "X" else "O"} is at (${if (isXMove) xPos._1 else oPos._1},${if (isXMove) xPos._2 else oPos._2}). Please enter a position (example = 1,1) or 'stay' to remain in the same position")
//
//    val input = StdIn.readLine().toLowerCase()
//    if (input == "stay") {
////      checkAttack(if (isXMove) oPos else xPos)
////      checkRangedAttack(if (isXMove) oPos else xPos)
////      printMap()
//      isXMove = !isXMove
//      getPlayerMove(currentPlayer, isXMove)
//    } else {
//      try {
//        val positions = input.split(",")
//        val x = positions(0).trim.toInt
//        val y = positions(1).trim.toInt
//
//        if (x < 1 || x > 40 || y < 1 || y > 40) {
//          println("Position out of range. Please enter coordinates within 1 to 40.")
//          getPlayerMove(currentPlayer, isXMove)
//        } else if ((isXMove && map(x)(y) == "O") || (!isXMove && map(x)(y) == "X")) {
//          println("That position is already taken by the opposing player. Please enter a different coordinate.")
//          getPlayerMove(currentPlayer, isXMove)
//        } else {
//          val (curX, curY) = if (isXMove) xPos else oPos
//          val distance = Math.abs(curX - x) + Math.abs(curY - y)
//
//          if (distance > currentCharacter.movement) {
//            println(s"The distance is greater than ${currentCharacter.movement}. Please enter a valid coordinate.")
//            getPlayerMove(currentPlayer, isXMove)
//          } else {
//            bfsShortestPath(if (isXMove) xPos else oPos, (x, y), currentCharacter.movement) match {
//              case Some(shortestPath) =>
//                shortestPath.foreach { case (newX, newY) =>
//                  println(s"New coordinates: ($newX, $newY)")
//                  if (isXMove) {
//                    map(xPos._1)(xPos._2) = ""
//                    xPos = (newX, newY)
//                    map(xPos._1)(xPos._2) = "X"
//                    if (!gameStartedX) gameStartedX = true
//                  } else {
//                    map(oPos._1)(oPos._2) = ""
//                    oPos = (newX, newY)
//                    map(oPos._1)(oPos._2) = "O"
//                    if (!gameStartedO) gameStartedO = true
//                  }
//                  if (isXMove) {
//                    checkAttack(xPos)
//                    checkRangedAttack(xPos)
//                  } else {
//                    checkAttack(oPos)
//                    checkRangedAttack(oPos)
//                    printMap()
//                  }
//                }
//                isXMove = !isXMove
//                (x, y)
//              case None =>
//                println("No valid path within movement limit. Please enter a new coordinate.")
//                getPlayerMove(currentPlayer, isXMove)
//            }
//          }
//        }
//      } catch {
//        case _: Throwable =>
//          println("Invalid input, please try again")
//          getPlayerMove(currentPlayer, isXMove)
//      }
//    }
//  }
//
//  def bfsShortestPath(start: (Int, Int), end: (Int, Int), maxMovement: Int): Option[List[(Int, Int)]] = {
//    val moves = List((0, 1), (0, -1), (1, 0), (-1, 0)) //possible moves (up, down, left, right) in the grid
//
//    val visited = Array.fill(40, 40)(false) // 2D array to keep track of visited coordinates, sets all values to false
//    val path = Array.fill(40, 40)((-1, -1)) // 2D array to keep track of the path taken. nitializes a 2D array of size 40x40 where each element is a tuple (-1, -1)
//
//    val queue = Queue[(Int, Int)]() // mutable collection based on FIFO
//    val steps = Array.fill(40, 40)(0) // Track path length instead of steps in the queue
//
//    queue.enqueue(start) //add an element to the end of the queue
//    visited(start._1)(start._2) = true
//
//    while (queue.nonEmpty) {
//      val current = queue.dequeue() //remove and retrieve the element at the front of the queue
//
//      if (current == end) { //urrent position being examined in the breadth-first search equals the target destination
//        val shortestPath = scala.collection.mutable.ListBuffer[(Int, Int)]()
//        var currentPos = end
//        var pathLength = 0 // Initialize path length counter
//
//        while (currentPos != start) {
//          shortestPath.prepend(currentPos) //The currentPos is added to the beginning of the shortestPath list. This operation adds the current position to the front of the list, forming the shortest path from the end to the start position.
//          pathLength += 1 // Increment path length for each step
//          currentPos = path(currentPos._1)(currentPos._2) // t updates the currentPos to the position stored in the path array at the coordinates specified by the current currentPos (currentPos._1 and currentPos._2). This step moves backward in the path from the current position to its predecessor along the shortest path.
//        }
//        ///The loop continues until the currentPos reaches the start position, effectively reconstructing the shortest path from the end to the start position by backtracking through the path array.
//
//        shortestPath.prepend(start) // adds the start position to the beginning of the shortestPath list.
//        // path is constructed in reverse order during the backtracking process.
//        // (start) is the last element added to the shortestPath list when tracing back the path.
//        if (pathLength <= maxMovement) {
//          // Print the shortest path coordinates if within movement limit
//          println("Shortest Path Coordinates:")
//          shortestPath.foreach(println)
//
//          return Some(shortestPath.toList) //converts the ListBuffer shortestPath to a regular immutable List
//        }
//      }
//
//      for ((dx, dy) <- moves) { ///performs a series of checks to explore potential movements in different directions from the current position
//        val newX = current._1 + dx
//        val newY = current._2 + dy
//
//        if (newX >= 0 && newX < 40 && newY >= 0 && newY < 40 && // checks boundaries
//          !visited(newX)(newY) && //avoids revisiting already explored positions, preventing infinite loop
//          map(newX)(newY) != "X" && map(newX)(newY) != "O" && // avoids enemy pieces and blocking cells
//          map(newX)(newY) != "-" && map(newX)(newY) != "|" &&
//          !horizontalBlockedCoordinates.contains((newX, newY)) && // duplicated blocking cells
//          !verticalBlockedCoordinates.contains((newX, newY)) &&
//          steps(current._1)(current._2) + 1 <= maxMovement) { // Check path length
//          queue.enqueue((newX, newY)) ///adds it as visited
//          visited(newX)(newY) = true //marks it as visited
//          path(newX)(newY) = current // updates current position
//          steps(newX)(newY) = steps(current._1)(current._2) + 1 // Update path length
//        }
//      }
//    }
//
//    None // If no path found within movement limit
//  }
//}
////
////  var continueGame = true
////  var isXMove = true
////
////  while (continueGame) {
////    val currentPlayer = if (isXMove) "Player X" else "Player O"
////
////    printMap()
////
////    val (x, y) = getPlayerMove(currentPlayer, isXMove)
