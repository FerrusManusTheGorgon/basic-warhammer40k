package warhammer.game

object CoordinateConverter {
  def convertCoordinates(coord: (Int, Int)): (Int, Int) = {
    val convertedCoord = (coord._2 - 1, coord._1)
    convertedCoord
  }

  def main(args: Array[String]): Unit = {
    // Example usage
    val originalCoord = (2, 3)
    val convertedCoord = convertCoordinates(originalCoord)

    println(s"Original Coordinates: $originalCoord")
    println(s"Converted Coordinates: $convertedCoord")
  }
}
