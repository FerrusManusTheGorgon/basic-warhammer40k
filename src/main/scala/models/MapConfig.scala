package models

import game.{Coordinates, GameUnit}

case class MapConfig(
                      horizontalLength: Int,
                      verticalLength: Int,
                      blocker: List[Coordinates]
                    ) {
  val EMPTY_SQUARE: String = " "
  val BLOCKED_SQUARE: String = "X"
  val HORIZONTAL_BORDER: String = "------+" + ("- - -+" * horizontalLength)
  val HORIZONTAL_RANGE: Range = 1 until horizontalLength + 1
  val VERTICAL_RANGE: Range = 1 until verticalLength + 1

  def createCoordinates: Map[Coordinates, String] = {
    val allCoordinates = HORIZONTAL_RANGE.flatMap { x =>
      VERTICAL_RANGE.map { y =>
        Coordinates(x, y)
      }.toList
    }.toList

    allCoordinates.foldLeft(Map.empty[Coordinates, String]) { (accumulator, coords) =>
      accumulator + (coords -> EMPTY_SQUARE)
    }
  }

  def layout: Map[Coordinates, String] = {
    blocker.foldLeft(createCoordinates) { (accumulator, coords) =>
      accumulator + (coords -> BLOCKED_SQUARE)
    }
  }

  def isWithinBounds(coordinates: Coordinates): Boolean = {
    coordinates.x >= 1 && coordinates.x <= horizontalLength &&
      coordinates.y >= 1 && coordinates.y <= verticalLength
  }

  //  def printMap(
  //                activePlayerUnit: GameUnit,
  //                passivePlayerUnit: GameUnit,
  //                includeActiveMovementRange: Boolean = false,
  //                includeActiveShootingRange: Boolean = false
  //              ): Unit = {
  //    val movementRange = if (includeActiveMovementRange) {
  //      Map.empty[Coordinates, String]
  //    } else Map.empty[Coordinates, String]
  //
  //    val shootingRange = if (includeActiveMovementRange) {
  //      Map.empty[Coordinates, String]
  //    } else Map.empty[Coordinates, String]
  //
  //    val boardState = layout + (
  //      activePlayerUnit.coordinates -> activePlayerUnit.character.avatar,
  //      passivePlayerUnit.coordinates -> passivePlayerUnit.character.avatar
  //    ) ++ movementRange
  //      ++ shootingRange
  //
  //    println(HORIZONTAL_BORDER)
  //
  //    VERTICAL_RANGE.reverse.foreach { y =>
  //      val row = HORIZONTAL_RANGE.map { x =>
  //        s"|  ${boardState(Coordinates(x, y))}  "
  //      }.reduce((a, b) => a + b)
  //      println(f"$y%4d  " + row + "|")
  //      println(HORIZONTAL_BORDER)
  //    }
  //
  //    println("      " + HORIZONTAL_RANGE.map(x => f"$x%4d").mkString("  "))
  //  }

}

