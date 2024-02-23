package jobs


import game.{GameUnit}
import models.{Board, Characters, GameCharacter, MapConfig, Maps}
import models.Coordinates
import scala.io.StdIn
import scala.collection.mutable.Queue
import scalacache._
import scalacache.modes.sync._

class MovementManager(implicit cache: Cache[Board]) {





  def moveUnits(units: List[GameUnit], map: MapConfig, passivePlayers: List[GameUnit]): List[GameUnit] = {

    @scala.annotation.tailrec
    def moveUnitsHelper(units: List[GameUnit], acc: List[GameUnit], passivePlayers: List[GameUnit]): List[GameUnit] = units match {
      case Nil => acc.reverse // Reverse the accumulator to maintain the original order
      case unit :: remainingUnits =>
        println(s"Move ${unit.character.avatar} or Hold Your Ground. Enter coordinates (format: x y) or Hold Your Ground")
        val input: String = StdIn.readLine() // Get user input for coordinates
        // Check if the input is "Hold Your Ground"
        if (input.toLowerCase == "hold your ground") {
          // If the player wants to hold their ground, add the current unit to the accumulator
          moveUnitsHelper(remainingUnits, unit :: acc, passivePlayers)
        } else {
          // If the input is not "Hold Your Ground", parse the coordinates and proceed as usual
          parseCoordinates(input) match {
            case Some(newCoordinates) =>
              if (isValidMove(map, newCoordinates, unit, passivePlayers)) {
                // Add the current unit with updated coordinates to the accumulator
                moveUnitsHelper(remainingUnits, unit.copy(coordinates = newCoordinates) :: acc, passivePlayers)
              } else {
                // If the move is not valid, ask the player to enter new coordinates
                println("Invalid coordinates. Please enter valid coordinates.")
                moveUnitsHelper(units, acc, passivePlayers)
              }
            case None =>
              // If the input cannot be parsed, ask the player to enter coordinates again
              println("Invalid input. Please enter coordinates in the format: x y")
              moveUnitsHelper(units, acc, passivePlayers)
          }
        }
    }

    // Pass an empty string as input to start the movement loop
    moveUnitsHelper(units, List.empty, passivePlayers)
  }

  def parseCoordinates(input: String): Option[Coordinates] = {
    val coordinates = input.split(" ")
    if (coordinates.length == 2) {
      try {
        val x = coordinates(0).toInt
        val y = coordinates(1).toInt
        Some(Coordinates(x, y))
      } catch {
        case _: NumberFormatException => None // If parsing fails, return None
      }
    } else {
      None // If the input format is incorrect, return None
    }
  }

  def isValidMove(map: MapConfig, newCoordinates: Coordinates, activePlayerUnit: GameUnit, passivePlayers: List[GameUnit]): Boolean = {
    map.isWithinBounds(newCoordinates) && passivePlayers.forall { passivePlayer =>
      getShortestPath(map, newCoordinates, activePlayerUnit, passivePlayer).isDefined
    }
  }

//  def isValidMove2(map: MapConfig, newCoordinates: Coordinates, activePlayerUnit: GameCharacter, passivePlayers: List[GameCharacter]): Boolean = {
//    map.isWithinBounds(newCoordinates) && passivePlayers.forall { passivePlayer =>
//      getShortestPath(map, newCoordinates, activePlayerUnit, passivePlayer).isDefined
//    }
//  }

  //TODO
  def getShortestPath(map: MapConfig, newCoordinates: Coordinates, activePlayerUnit: GameUnit, passivePlayerUnit: GameUnit): Option[List[Coordinates]] = {
    val start = activePlayerUnit.coordinates
    val end = newCoordinates
    val maxMovement = activePlayerUnit.character.movement
    val moves = List((0, 1), (0, -1), (1, 0), (-1, 0)) //possible moves (up, down, left, right
    val visited = Array.fill(map.horizontalLength + 2, map.verticalLength + 2)(false) //2D array to keep track of visited cells
    val path = Array.fill(map.horizontalLength + 2, map.verticalLength + 2)(Coordinates(-1, -1)) //2D array to store the parent of each cell in the shortest path
    val queue = Queue[Coordinates]() //queue to perform Breadth-First Search (BFS)
    val steps = Array.fill(map.horizontalLength + 2, map.verticalLength + 2)(0) //2D array to store the number of steps taken to reach each cell
    queue.enqueue(start)
    visited(start.x)(start.y) = true
    while (queue.nonEmpty) {
      val current = queue.dequeue()
      println(s"Exploring cell: $current")
      if (current == end) {
        val shortestPath = scala.collection.mutable.ListBuffer[Coordinates]()
        var currentPos = end
        var pathLength = 0
        while (currentPos != start) {
          shortestPath.prepend(currentPos)
          pathLength += 1
          currentPos = path(currentPos.x)(currentPos.y)
        }
        shortestPath.prepend(start) //add an element at the beginning of the list
        println(s"Path length: $pathLength, Max movement: $maxMovement")
        if (pathLength <= maxMovement) {
          println("Shortest Path Coordinates:")
          shortestPath.foreach(println)
          return Some(shortestPath.toList) //Some is to indicate that a valid result (the shortest path)
        }
      }
      for ((dx, dy) <- moves) { //loop iterating over each pair (dx, dy) in the moves list.
        val newX = current.x + dx // moves represents possible moves in terms of changes in x and y coordinates (e.g., moving up, down, left, or right)
        val newY = current.y + dy
        if (map.isWithinBounds(Coordinates(newX, newY)) && //checks whether the new coordinates are within the bounds of the map
          !visited(newX)(newY) && //Checks if the cell with the new coordinates has not been visited before
          map.layout.getOrElse(Coordinates(newX, newY), "") != map.BLOCKED_SQUARE && //Checks if the cell with the new coordinates is not blocked on the map
          steps(current.x)(current.y) + 1 <= maxMovement) { //Ensures that the total number of steps taken so far is within the maximum allowed movement
          println(s"Enqueuing cell: ($newX, $newY)")
          queue.enqueue(Coordinates(newX, newY)) //dds the new coordinates to the BFS queue for further exploration.
          visited(newX)(newY) = true //Marks the cell as visited
          path(newX)(newY) = current //Records the path from the current cell to the new cell.
          steps(newX)(newY) = steps(current.x)(current.y) + 1 //Updates the number of steps taken to reach the new cell
        }
      }
    }
    None
  }

//  def getShortestPath2(map: MapConfig, newCoordinates: Coordinates, activePlayerUnit: GameCharacter, passivePlayerUnit: GameCharacter): Option[List[Coordinates]] = {
//    val start = activePlayerUnit.coordinates
//    val end = newCoordinates
//    val maxMovement = activePlayerUnit.character.movement
//    val moves = List((0, 1), (0, -1), (1, 0), (-1, 0)) //possible moves (up, down, left, right
//    val visited = Array.fill(map.horizontalLength + 2, map.verticalLength + 2)(false) //2D array to keep track of visited cells
//    val path = Array.fill(map.horizontalLength + 2, map.verticalLength + 2)(Coordinates(-1, -1)) //2D array to store the parent of each cell in the shortest path
//    val queue = Queue[Coordinates]() //queue to perform Breadth-First Search (BFS)
//    val steps = Array.fill(map.horizontalLength + 2, map.verticalLength + 2)(0) //2D array to store the number of steps taken to reach each cell
//    queue.enqueue(start)
//    visited(start.x)(start.y) = true
//    while (queue.nonEmpty) {
//      val current = queue.dequeue()
//      println(s"Exploring cell: $current")
//      if (current == end) {
//        val shortestPath = scala.collection.mutable.ListBuffer[Coordinates]()
//        var currentPos = end
//        var pathLength = 0
//        while (currentPos != start) {
//          shortestPath.prepend(currentPos)
//          pathLength += 1
//          currentPos = path(currentPos.x)(currentPos.y)
//        }
//        shortestPath.prepend(start) //add an element at the beginning of the list
//        println(s"Path length: $pathLength, Max movement: $maxMovement")
//        if (pathLength <= maxMovement) {
//          println("Shortest Path Coordinates:")
//          shortestPath.foreach(println)
//          return Some(shortestPath.toList) //Some is to indicate that a valid result (the shortest path)
//        }
//      }
//      for ((dx, dy) <- moves) { //loop iterating over each pair (dx, dy) in the moves list.
//        val newX = current.x + dx // moves represents possible moves in terms of changes in x and y coordinates (e.g., moving up, down, left, or right)
//        val newY = current.y + dy
//        if (map.isWithinBounds(Coordinates(newX, newY)) && //checks whether the new coordinates are within the bounds of the map
//          !visited(newX)(newY) && //Checks if the cell with the new coordinates has not been visited before
//          map.layout.getOrElse(Coordinates(newX, newY), "") != map.BLOCKED_SQUARE && //Checks if the cell with the new coordinates is not blocked on the map
//          steps(current.x)(current.y) + 1 <= maxMovement) { //Ensures that the total number of steps taken so far is within the maximum allowed movement
//          println(s"Enqueuing cell: ($newX, $newY)")
//          queue.enqueue(Coordinates(newX, newY)) //dds the new coordinates to the BFS queue for further exploration.
//          visited(newX)(newY) = true //Marks the cell as visited
//          path(newX)(newY) = current //Records the path from the current cell to the new cell.
//          steps(newX)(newY) = steps(current.x)(current.y) + 1 //Updates the number of steps taken to reach the new cell
//        }
//      }
//    }
//    None
//  }

//  def httpMove(move: Coordinates, boardId: String): String = {
//    // Retrieve the cached board using the provided boardId
//    val cachedBoard: IO[Option[Board]] = cache.flatMap(_.get(boardId))
//
//    // Evaluate the IO action
//    val result: Option[Board] = cachedBoard.unsafeRunSync()
//result.map(_.print).getOrElse(":(")
def httpMove(move: Coordinates, boardId: String): String = {
  // Retrieve the cached board using the provided boardId
  val cachedBoard: Option[Board] = sync.get(boardId)
cachedBoard.map(_.print).getOrElse(";)")
}




//    result match {
//      case Some(board) =>
//        // Get the active player's units from the board
//        val activePlayerUnits = if (board.isPlayer1Turn) board.player1 else board.player2
//
//        // Find the unit belonging to the active player at the specified coordinates
//        val unitToUpdate = activePlayerUnits.find(_.currentPosition == move)
//
//        unitToUpdate match {
//          case Some(unit) =>
//            // Check if the move is valid
//            val isValid = isValidMove(board.map, move, unit, board.getPassivePlayers)
//
//            if (isValid) {
//              // Update the character's coordinates with the new move
//              val updatedUnit = unit.copy(coordinates = move)
//
//              // Create a new board with the updated character's coordinates and the same boardId
//              val updatedBoard = board.copy(
//                player1 = if (board.isPlayer1Turn) board.updatePlayer1Unit(updatedUnit) else board.player1,
//                player2 = if (!board.isPlayer1Turn) board.updatePlayer2Unit(updatedUnit) else board.player2
//              )
//
//              // Cache the updated board
//              cache.flatMap(_.put(boardId)(updatedBoard)).unsafeRunSync()
//
//              // Return a success message
//              s"Successfully moved ${updatedUnit.avatar} to coordinates: $move"
//            } else {
//              // If the move is not valid, return an error message
//              "Invalid move. Please choose a valid move."
//            }
//          case None =>
//            // If no unit is found at the specified coordinates, return an error message
//            "No unit found at the specified coordinates."
//        }
//      case None =>
//        // If the board with the provided boardId is not found in the cache, return an error message
//        "Board not found. Please provide a valid boardId."
//    }
  }



  // pull the board from cache with boardId
  // check if valid move
  //if not valid prompt player to retype a valid move
  //create new board with same boardId
  // update character




