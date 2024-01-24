import game.{Coordinates, GameUnit}
import models.{Characters, GameCharacter, MapConfig, Maps}
import scala.io.StdIn

object Main extends App {

  //define game inputs
  //map

  /**
   * what are our inputs
   * player 1 that has army x with 2 units
   * player 2 army y with 3 units
   *
   * deployment phase
   * ......
   *
   * ------------------
   * assumptions
   *
   * we have two players that have 1 model that each has a given position
   *
   * we know which map we are using
   *
   * So now the game can start. It takes these arguments, character1, location1, character2, location2, map, who won the coin toss
   *
   *
   *
   *
   *
   */



 val ALIVE_STATE: String = "alive"
  val DEAD_STATE: String = "dead"


  val player1Unit: GameCharacter = Characters.SpaceMarine
  val player1UnitLocation: Coordinates = Coordinates(3, 4)

  val unit1: GameUnit = GameUnit(
    character = player1Unit,
    coordinates = player1UnitLocation,
    state = ALIVE_STATE
  )

  val player2Unit: GameCharacter = Characters.Ork
  val player2UnitLocation: Coordinates = Coordinates(6, 8)




  val unit2: GameUnit = GameUnit(
    character = player2Unit,
    coordinates = player2UnitLocation,
    state = ALIVE_STATE
  )

  val mapWeAreUsing = Maps.RockyDivide
  val isPlayer1First = true

  start(unit1, unit2, mapWeAreUsing, isPlayer1First)

  def start(
             unit1: GameUnit,
             unit2: GameUnit,
             map: MapConfig,
             isPlayer1First: Boolean
           ) : Unit = {

    movement(map, unit1, unit2)

    //map.layout



  }


  def movement(map: MapConfig, activePlayerUnit: GameUnit, passivePlayerUnit: GameUnit): GameUnit = {
    val input: String = StdIn.readLine()
    // Assuming Coordinates has a constructor that takes two Int parameters
    val newCoordinates: Coordinates = parseCoordinates(input)
    movementHelper(map, newCoordinates, activePlayerUnit)
  }

  // Parse string input into Coordinates because "input: String = StdIn.readLine()" is a string
  def parseCoordinates(input: String): Coordinates = {
    val Array(x, y) = input.split("\\s+").map(_.toInt)
    Coordinates(x, y)
  }

  @scala.annotation.tailrec
  private def movementHelper(map: MapConfig, newCoordinates: Coordinates, currentUnit: GameUnit): GameUnit = {
    if (isValidMove(map, newCoordinates)) {
      // If the move is valid, return the current GameUnit with updated coordinates
      currentUnit.copy(coordinates = newCoordinates)
    } else {
      // If the move is not valid, return the current GameUnit unchanged
      currentUnit
    }
  }

  private def isValidMove(map: MapConfig, newCoordinates: Coordinates): Boolean = {
    map.isWithinBounds(newCoordinates) && bfsShortestPath(map, activePlayerUnit, passivePlayerUnit, maxMovement).isDefined
  }


  def bfsShortestPath(map: MapConfig, activePlayerUnit: GameUnit, passivePlayerUnit: GameUnit, maxMovement: Int): Option[List[Coordinates]] = {
    val start = activePlayerUnit.coordinates
    val end = passivePlayerUnit.coordinates

    val moves = List((0, 1), (0, -1), (1, 0), (-1, 0)) //possible moves (up, down, left, right

    val visited = Array.fill(map.horizontalLength + 2, map.verticalLength + 2)(false)//2D array to keep track of visited cells
    val path = Array.fill(map.horizontalLength + 2, map.verticalLength + 2)(Coordinates(-1, -1))//2D array to store the parent of each cell in the shortest path
    val queue = Queue[Coordinates]()//queue to perform Breadth-First Search (BFS)
    val steps = Array.fill(map.horizontalLength + 2, map.verticalLength + 2)(0)//2D array to store the number of steps taken to reach each cell

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
          queue.enqueue(Coordinates(newX, newY))//dds the new coordinates to the BFS queue for further exploration.
          visited(newX)(newY) = true //Marks the cell as visited
          path(newX)(newY) = current //Records the path from the current cell to the new cell.
          steps(newX)(newY) = steps(current.x)(current.y) + 1 //Updates the number of steps taken to reach the new cell
        }
      }
    }

    None
  }


}



    /**
     * print board
     *  We could print valid moves for our user
     * ask for input
     * check if input is valid
     * if valid
     *    update activePlayerUnit
     *    print board
     *    return updated activePlayerUnit
     * if invalid
     *    we want to retry ask for input
     */
//    map.printMap(unit1, unit2)
    printBoard(map, unit1, unit2)

    activePlayerUnit

  }


  def printBoard(
                map: MapConfig,
                activePlayerUnit: GameUnit,
                passivePlayerUnit: GameUnit,
                includeActiveMovementRange: Boolean = false,
                includeActiveShootingRange: Boolean = false
              ): Unit = {
    val currentMap = map.layout + (
      activePlayerUnit.coordinates -> activePlayerUnit.character.avatar,
      passivePlayerUnit.coordinates -> passivePlayerUnit.character.avatar
    )

    val movementRange = if (includeActiveMovementRange) {
      //call method to check range here
      //input unit (need current location, and movement), and currentMap: returns Map[Coordinates, String]
      Map.empty[Coordinates, String]
    } else Map.empty[Coordinates, String]

    val shootingRange = if (includeActiveMovementRange) {
      Map.empty[Coordinates, String]
    } else Map.empty[Coordinates, String]

    val boardState = currentMap
      ++ movementRange
      ++ shootingRange

    println(map.HORIZONTAL_BORDER)

    map.VERTICAL_RANGE.reverse.foreach { y =>
      val row = map.HORIZONTAL_RANGE.map { x =>
        s"|  ${boardState(Coordinates(x, y))}  "
      }.reduce((a, b) => a + b)
      println(f"$y%4d  " + row + "|")
      println(map.HORIZONTAL_BORDER)
    }

    println("      " + map.HORIZONTAL_RANGE.map(x => f"$x%4d").mkString("  "))
  }

}

