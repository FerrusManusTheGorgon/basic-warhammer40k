package models

import game.{Coordinates, GameUnit}

case class MapConfig(
                      horizontalLength: Int,
                      verticalLength: Int,
                      blocker: List[Coordinates]
                    ) {
  val EMPTY_SQUARE = " "
  val BLOCKED_SQUARE = "X"

  def createCoordinates: Map[Coordinates, String] = {
    val horizontalRange = 1 until horizontalLength + 1
    val verticalRange = 1 until verticalLength + 1

    val allCoordinates = horizontalRange.flatMap { x =>
      verticalRange.map { y =>
        Coordinates(x, y)
      }.toList
    }.toList

    allCoordinates.foldLeft(Map.empty[Coordinates, String]) { (accumulator, coords) =>
      accumulator + (coords -> EMPTY_SQUARE)
    }
  }

  def layout = {
    blocker.foldLeft(createCoordinates) { (accumulator, coords) =>
      accumulator + (coords -> BLOCKED_SQUARE)
    }
  }
  
  def printMap(
                activePlayerUnit: GameUnit,
                passivePlayerUnit: GameUnit,
                includeActiveMovementRange: Boolean = false,
                includeActiveShootingRange: Boolean = false
              ): Unit = {
    val boardState = layout + (
      activePlayerUnit.coordinates -> activePlayerUnit.character.avatar,
      passivePlayerUnit.coordinates -> passivePlayerUnit.character.avatar
    )
    // Print top row numbers

    println("------+" + ("- - -+" * verticalLength))

    val horizontalRange = 1 until horizontalLength + 1
    val verticalRange = 1 until verticalLength + 1

    verticalRange.reverse.foreach { y =>
      val printString = horizontalRange.map { x =>
        s"|  ${boardState(Coordinates(x, y))}  "
      }.reduce((a, b) => a + b)
      println(f"$y%4d  " + printString + "|")
      println("------+" + ("- - -+" * verticalLength))
    }

    println("      " + (1 until verticalLength + 1).map(i => f"$i%4d").mkString("  "))
  }
  
}

