package jobs

import models.{Character, Characters, Maps}

class GameMap(mapData: Map[String, Any]) {
  val horizontalLength: Int = mapData("HorizontalLength").asInstanceOf[Int]
  val verticalLength: Int = mapData("VerticalLength").asInstanceOf[Int]
  val blockedCoordinates: List[(Int, Int)] = mapData("Blocker").asInstanceOf[List[(Int, Int)]]
  // Define initial positions
  val spaceMarinePos = (1, 2)
  val orkPos = (4, 5)

  val spaceMarine: Character = Characters.SpaceMarine
  val ork: Character = Characters.Ork

  val map: Array[Array[String]] = Array.fill(horizontalLength + 4, verticalLength + 4)("")

  // New method for coordinate conversion
  def convertCoordinates(coord: (Int, Int)): (Int, Int) = {
    ((coord._2 - 0), coord._1 - 1)
  }

  def initializeMap(sPos: (Int, Int), oPos: (Int, Int), spaceMarine: Character, ork: Character): Unit = {
    for (i <- 0 until horizontalLength; j <- 0 until verticalLength)
      map(i)(j) = ""

    // Place SpaceMarine at the initial position using avatar "S"
    val convertedSpaceMarinePos = convertCoordinates(sPos)
    map(convertedSpaceMarinePos._1)(convertedSpaceMarinePos._2) = spaceMarine.avatar

    // Place Ork at the initial position using avatar "O"
    val convertedOrkPos = convertCoordinates(oPos)
    map(convertedOrkPos._1)(convertedOrkPos._2) = ork.avatar

    // Place horizontal blockers
    for ((col, row) <- blockedCoordinates) {
      val convertedBlockedCoord = convertCoordinates((col, row))
      map(convertedBlockedCoord._1)(convertedBlockedCoord._2) = "X"
    }
  }

  def printMap(): Unit = {
    // Print top row numbers
    println("" + (0 until verticalLength).map(i => f"$i%4d").mkString("  "))
    println("------+" + ("-----+" * verticalLength))
    for (i <- 1 until horizontalLength) {
      print(f"$i%4d  | ")
      for (j <- 0 until verticalLength) {
        map(i)(j) match {
          case Characters.SpaceMarine.avatar => print(s" ${Characters.SpaceMarine.avatar}  | ")
          case Characters.Ork.avatar => print(s" ${Characters.Ork.avatar}  | ")
          case "X" => print(" X  | ") // Represents a blocked coordinate
          case _ => print("    | ")
        }
      }
      println("\n------+" + ("-----+" * verticalLength))
    }

    // Print the list of coordinates and contents (including blocked coordinates)
    println("\nCell Contents and Coordinates:")
    for {
      i <- 1 until horizontalLength + 1
      j <- 1 until verticalLength + 1
    } {
      println(s"($j, $i): ${map(j + 0)(i + 0)}")
    }

  }
}



//// Usage
//val mapConfig = Map(
//  "HorizontalLength" -> 3,
//  "VerticalLength" -> 3,
//  "Blocker" -> List((1, 1)) // Coordinates for a single blocked position
//)

/*
val gameMap = new GameMap(mapConfig)

// Set initial positions for SpaceMarine and Ork
val sPos: (Int, Int) = (0, 0)
val oPos: (Int, Int) = (2, 2)

gameMap.initializeMap(sPos, oPos, Characters.SpaceMarine, Characters.Ork)
gameMap.printMap()*/
