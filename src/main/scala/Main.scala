import game.{Coordinates, GameUnit}
import jobs.{CheckVictoryConditions, RangeAttackManager2, CloseCombatManager}
import models.{Characters, GameCharacter, MapConfig, Maps}

import scala.collection.mutable.Queue
import scala.io.StdIn
import scala.annotation.tailrec
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

  val player2BigShootaUnit: GameCharacter = Characters.OrkWithBigShoota
  val player2BigShootaUnitLocation: Coordinates = Coordinates(8, 10)

  val unit3: GameUnit = GameUnit(
    character = player2BigShootaUnit,
    coordinates = player2BigShootaUnitLocation,
    state = ALIVE_STATE
  )

  val spaceMarineUnits: List[GameUnit] = List(unit1)
  val orkUnits: List[GameUnit] = List(unit2, unit3)
  val player1Units: List[GameUnit] = List(unit1)
  val player2Units: List[GameUnit] = List(unit2, unit3)
  val allUnits: List[GameUnit] = player1Units ++ player2Units


  val mapWeAreUsing = Maps.RockyDivide
  val isPlayer1First = true


  val checkRangedAttack = new RangeAttackManager2
  val checkCloseCombatAttack = new CloseCombatManager




  //  // Check if a ranged attack is possible
  //  RangeAttackManager2.checkRangedAttack(unit1.coordinates, unit2.coordinates)
  //
  //  // Perform a ranged attack
  //  RangeAttackManager2.performRangedAttack(unit2.coordinates)


  val victoryChecker = new CheckVictoryConditions
  //rangeattackMamnager and attacker manager need rto look like victory checker
  start(player1Units, player2Units, mapWeAreUsing, isPlayer1First)



  // Create an instance of CloseCombatManager
  //  val closeCombatManager = ??? //new CloseCombatManager(mapWeAreUsing, unit1, unit2)
  //  val rangeAttackManager = ??? //new RangeAttackManager2(mapWeAreUsing, unit1, unit2)


  import scala.annotation.tailrec
  import scala.io.StdIn

  def moveUnits(units: List[GameUnit], map: MapConfig): List[GameUnit] = {
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



  //
  //
  //  /**
  //   * print board
  //   * We could print valid moves for our user
  //   * ask for input
  //   * check if input is valid
  //   * if valid
  //   * update activePlayerUnit
  //   * print board
  //   * return updated activePlayerUnit
  //   * if invalid
  //   * we want to retry ask for input
  //   */
  //  //    map.printMap(unit1, unit2)
  //  printBoard(map, unit1, unit2)
  //
  //  activePlayerUnit


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

  def start(
             activePlayerUnits: List[GameUnit],
             passivePlayerUnits: List[GameUnit],
             map: MapConfig,
             isPlayer1First: Boolean
           ): Unit = {
    @tailrec
    def moveAllUnits(units: List[GameUnit], movedUnits: List[GameUnit]): List[GameUnit] = units match {
      case Nil => movedUnits // If all units have been moved, return the list of moved units
      case unit :: remainingUnits =>
        printBoard(map, activePlayerUnits, passivePlayerUnits) // Print the current state of the map
        val movedUnit :: restUnits = moveUnits(unit :: remainingUnits, map) // Move the first unit from the remaining units
        val updatedMovedUnits = movedUnit :: movedUnits // Add the moved unit to the accumulator
        printBoard(map, updatedMovedUnits, passivePlayerUnits, includeActiveMovementRange = false, includeActiveShootingRange = false) // Print the updated map with the moved unit
        moveAllUnits(restUnits, updatedMovedUnits) // Recursively move the remaining units
    }

    val finalMovedUnits = moveAllUnits(activePlayerUnits, List.empty) // Start moving all units of the active player

    val finalMapState = turn(finalMovedUnits, passivePlayerUnits, map) // Call the turn function with the final moved units and the passive player's units
    printBoard(finalMapState, finalMovedUnits, passivePlayerUnits, includeActiveMovementRange = false, includeActiveShootingRange = false) // Print the final state of the map

    // Swap the active and passive player units
    val (newActivePlayerUnits, newPassivePlayerUnits) = (passivePlayerUnits, finalMovedUnits)

    // Call start with the new active and passive player units
    start(newActivePlayerUnits, newPassivePlayerUnits, finalMapState, !isPlayer1First)
  }


  def turn(
            activePlayerUnits: List[GameUnit],
            passivePlayerUnits: List[GameUnit],
            map: MapConfig
          ): MapConfig = { // Change return type to MapConfig
    // Print the current state of the map
    printBoard(map, activePlayerUnits, passivePlayerUnits)
    // Move the active player's units
    val movedUnits = moveUnits(activePlayerUnits, map)
    // Print the updated state of the map after movement
    printBoard(map, movedUnits, passivePlayerUnits, includeActiveMovementRange = false, includeActiveShootingRange = false)
    // Check for ranged attacks, perform if possible, and print the updated state of the map
    // val targetedUnits = checkRangedAttack.performRangedAttackIfInRange(map, movedUnits, passivePlayerUnits)
    // printBoard(map, movedUnits, targetedUnit)
    // Check for melee attacks, perform if possible, and print the updated state of the map
    // val potentialMeleeTarget = checkCloseCombatAttack.performCloseCombatAttackIfInRange(map, movedUnits, targetedUnit)
    // printBoard(map, movedUnits, potentialMeleeTarget)
    // Recursively call turn with the updated state of the game
    // turn(potentialMeleeTarget, passivePlayerUnits, map)
    map // Return the updated map state
  }



  // comment out bottom
    //    // comment out top
    //    val targetedUnits = checkRangedAttack.performRangedAttackIfInRange(map, movedUnits, player2Units)
    //    printBoard(map, movedUnits, targetedUnit)
    //    val potentialMeleeTarget = checkCloseCombatAttack.performCloseCombatAttackIfInRange(map, movedUnits, targetedUnit)
    //    printBoard(map, movedUnits, potentialMeleeTarget)
    //    // comment out bottom


    //    val sitedUnit = checkRangedAttack.checkRangedAttack(map, movedUnit, unit2)
    //    val shotUnit = checkRangedAttack.performRangedAttack(map, movedUnit, unit2)
    //    printBoard(map, movedUnit, shotUnit)

    //      val potentialTarget = checkRangedAttack.performRangedAttackIfInRange(map, movedUnit, unit2)

    //          val shotUnit = checkRangedAttack.performRangedAttack(map, movedUnit, unit2)
    //      printBoard(map, movedUnit, potentialTarget)

    //  // comment out top
    //val targetedUnit = checkRangedAttack.performRangedAttackIfInRange(map, movedUnits.head, player2Units.head)
    //  printBoard(map, movedUnits.head, targetedUnit)
    //  val potentialMeleeTarget = checkCloseCombatAttack.performCloseCombatAttackIfInRange(map, movedUnits.head, targetedUnit)
    //  printBoard(map, movedUnits.head, potentialMeleeTarget)
    //  // comment out bottom

    // comment out top
    //  victoryChecker.checkVictory(potentialMeleeTarget) match {
    //    case Some(result) => println(result)
    //      "In The Grim Darkness Of The Far Future There Is Only War"
    //    case None => turn(potentialMeleeTarget, map) // Pass the map argument here
    //  }
    //  // comment out bottom

  }







