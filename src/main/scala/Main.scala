import game.{Coordinates, GameUnit}
import jobs.{CheckVictoryConditions, CloseCombatManager, RangeAttackManager2}
import models.UnitState.ALIVE_STATE
import models.{Characters, GameCharacter, MapConfig, Maps}

import scala.collection.mutable.Queue
import scala.io.StdIn
import scala.annotation.tailrec

object Main extends App {
  //TODO
  /**
   * enhance cooredinates case class to have up down ledt right methods, an adjacent method
   * convert loops to use tail rec or fold left
   * ensure passive and active players are always included
   * move unit info into a different object
   * move all movement code into a separate class
   * 
   * 
   */
  
  

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

  val player2BigShootaUnit: GameCharacter = Characters.OrkWithBigShoota
  val player2BigShootaUnitLocation: Coordinates = Coordinates(8, 10)

  val unit3: GameUnit = GameUnit(
    character = player2BigShootaUnit,
    coordinates = player2BigShootaUnitLocation,
    state = ALIVE_STATE
  )


  val player1Units: List[GameUnit] = List(unit1)
  val player2Units: List[GameUnit] = List(unit2, unit3)
  val mapWeAreUsing = Maps.RockyDivide
  val isPlayer1First = true


  val checkRangedAttack = new RangeAttackManager2
  val checkCloseCombatAttack = new CloseCombatManager
  val victoryChecker = new CheckVictoryConditions

  start(player1Units, player2Units, mapWeAreUsing, isPlayer1First)

  def start(
             player1: List[GameUnit],
             player2: List[GameUnit],
             map: MapConfig,
             isPlayer1First: Boolean
           ): Unit = {

    if (isPlayer1First) turn(player1, player2, map)
    else turn(player2, player1, map)

  }


  @tailrec
  def turn(
            activeUnits: List[GameUnit],
            passiveUnits: List[GameUnit],
            map: MapConfig
          ): String = {
    printBoard(map, activeUnits, passiveUnits)
    val movedUnits = moveUnits(activeUnits, map)
    printBoard(map, movedUnits, passiveUnits)

    val victoryMessageO = victoryChecker.checkVictory(passiveUnits)

    if (victoryMessageO.isDefined) victoryMessageO.get
    else turn(passiveUnits, movedUnits, map)
  }

  def moveUnits(units: List[GameUnit], map: MapConfig): List[GameUnit] = {//TODO
    @tailrec
    def moveUnitsHelper(units: List[GameUnit], acc: List[GameUnit]): List[GameUnit] = units match {
      case Nil => acc.reverse // Reverse the accumulator to maintain the original order
      case unit :: remainingUnits =>
        println(s"Move ${unit.character.avatar} or Hold Your Ground. Enter coordinates (format: x y) or Hold Your Ground")
        val input: String = StdIn.readLine() // Get user input for coordinates
        // Check if the input is "Hold Your Ground"
        if (input.toLowerCase == "hold your ground") {
          // If the player wants to hold their ground, add the current unit to the accumulator
          moveUnitsHelper(remainingUnits, unit :: acc)
        } else {
          // If the input is not "Hold Your Ground", parse the coordinates and proceed as usual
          parseCoordinates(input) match {
            case Some(newCoordinates) =>
              if (isValidMove(map, newCoordinates, unit, remainingUnits.headOption.getOrElse(unit))) {
                // Add the current unit with updated coordinates to the accumulator
                moveUnitsHelper(remainingUnits, unit.copy(coordinates = newCoordinates) :: acc)
              } else {
                // If the move is not valid, ask the player to enter new coordinates
                println("Invalid coordinates. Please enter valid coordinates.")
                moveUnitsHelper(units, acc)
              }
            case None =>
              // If the input cannot be parsed, ask the player to enter coordinates again
              println("Invalid input. Please enter coordinates in the format: x y")
              moveUnitsHelper(units, acc)
          }
        }
    }

    // Pass an empty string as input to start the movement loop
    moveUnitsHelper(units, List.empty)
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


  def isValidMove(map: MapConfig, newCoordinates: Coordinates, activePlayerUnit: GameUnit, passivePlayerUnit: GameUnit): Boolean = {
    map.isWithinBounds(newCoordinates)
      && getShortestPath(map, newCoordinates, activePlayerUnit, passivePlayerUnit).isDefined
  }


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


  def printBoard(
                  map: MapConfig,
                  player1Units: List[GameUnit],
                  player2Units: List[GameUnit],
                  includeActiveMovementRange: Boolean = false,
                  includeActiveShootingRange: Boolean = false
                ): Unit = {
    val boardState = (player1Units ++ player2Units).foldLeft(map.layout) { (acc, unit) =>
      acc + (unit.coordinates -> unit.character.avatar)
    }

    val movementRange = if (includeActiveMovementRange) {
      // call method to check range here
      // input units (need current location, and movement), and currentMap: returns Map[Coordinates, String]
      Map.empty[Coordinates, String]
    } else Map.empty[Coordinates, String]

    val shootingRange = if (includeActiveShootingRange) {
      Map.empty[Coordinates, String]
    } else Map.empty[Coordinates, String]

    val finalState = boardState ++ movementRange ++ shootingRange

    println(map.HORIZONTAL_BORDER)

    map.VERTICAL_RANGE.reverse.foreach { y =>
      val row = map.HORIZONTAL_RANGE.map { x =>
        s"|  ${finalState.getOrElse(Coordinates(x, y), " ")}  "
      }.reduce((a, b) => a + b)
      println(f"$y%4d  " + row + "|")
      println(map.HORIZONTAL_BORDER)
    }

    println("      " + map.HORIZONTAL_RANGE.map(x => f"$x%4d").mkString("  "))
  }


}







