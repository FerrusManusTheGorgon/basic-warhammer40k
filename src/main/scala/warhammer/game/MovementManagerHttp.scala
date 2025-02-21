package warhammer.game

import warhammer.game.models.{Board, Coordinates, GameCharacter, MapConfig}
import scalacache._

import scala.collection.mutable.Queue


class MovementManagerHttp(implicit cache: Cache[Board]) {


//  def isValidMove(map: MapConfig, newCoordinates: Coordinates, activePlayerUnit: GameCharacter, passivePlayers: List[GameCharacter]): Boolean = {
//    map.isWithinBounds(newCoordinates) && passivePlayers.forall { passivePlayer =>
//      getShortestPath(map, newCoordinates, activePlayerUnit, passivePlayer).isDefined
//    }
//  }
def isValidMove(map: MapConfig,
                newCoordinates: Coordinates,
                activePlayerUnit: GameCharacter,
                passivePlayers: List[GameCharacter],
                activePlayers: List[GameCharacter]): Boolean = {
  val activeTeammates = activePlayers.filterNot(_ == activePlayerUnit) // Remove activePlayerUnit from the list
  val noConflictWithTeammates = activeTeammates.forall(_.currentPosition != newCoordinates) // Check for conflicts with teammates
  val validPathToPassivePlayers = passivePlayers.forall { passivePlayer =>
    getShortestPath(map, newCoordinates, activePlayerUnit, List(passivePlayer)).isDefined
  }

  map.isWithinBounds(newCoordinates) && noConflictWithTeammates && validPathToPassivePlayers
}


  //TODO
  def getShortestPath(map: MapConfig, newCoordinates: Coordinates, activePlayerUnit: GameCharacter, passivePlayers: List[GameCharacter]): Option[List[Coordinates]] = {
    val start = activePlayerUnit.currentPosition
    val end = newCoordinates
    val maxMovement = activePlayerUnit.movement
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
          map.layout.getOrElse(Coordinates(newX, newY), "") != map.BLOCKED_SQUARE &&  //Checks if the cell with the new coordinates is not blocked on the map
          !passivePlayers.exists(_.currentPosition == Coordinates(newX, newY)) && // checks if there are no passive players at the new coordinates
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


  def httpMove(moveCoordinates: Coordinates, board: Board, avatar: String): (Board, String) = {

    // Get the active player's units from the board
    val activePlayerUnits = if (board.isPlayer1Turn) board.player1 else board.player2
    val currentMoveUnitOption = activePlayerUnits.find(p => p.avatar == avatar && !p.movePhaseCompleted)
    currentMoveUnitOption match {
      case Some(currentUnit) =>
        if (moveCoordinates == currentUnit.currentPosition) {
          // Special case for coordinates (100, 100)
          val updatedUnit = currentUnit.copy(movePhaseCompleted = true)
          val updatedBoard = board.updateActiveUnit(updatedUnit)
          val boardString = updatedBoard.printBoard()
          (updatedBoard, s"$boardString\n$avatar held its ground")
        } else if (isValidMove(board.map, moveCoordinates, currentUnit, board.getPassivePlayers, board.getActivePlayers))
        {
          // Update the unit's position
          val updatedUnit = currentUnit.copy(currentPosition = moveCoordinates, movePhaseCompleted = true)
          val updatedBoard = board.updateActiveUnit(updatedUnit)
          val boardString = updatedBoard.printBoard()
          (updatedBoard, s"$boardString\n$updatedUnit") // Print the updated GameCharacter
        } else {
          (board, s"Invalid move for $avatar at coordinates: $moveCoordinates")
        }
      case None =>
        (board, s"No valid unit with avatar $avatar found for moving.")
    }

  }

}








