package warhammer.game.models

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


}

