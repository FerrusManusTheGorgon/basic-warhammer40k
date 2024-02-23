package jobs

import game.GameUnit
import models.{Coordinates, MapConfig}

object MapUtils {

  def generateBoardString(
                           map: MapConfig,
                           player1Units: List[GameUnit],
                           player2Units: List[GameUnit],
                           includeActiveMovementRange: Boolean = false,
                           includeActiveShootingRange: Boolean = false
                         ): String = {
    val boardState = (player1Units ++ player2Units).foldLeft(map.layout) { (acc, unit) =>
      acc + (unit.coordinates -> unit.character.avatar)
    }
    val movementRange = if (includeActiveMovementRange) {
      Map.empty[Coordinates, String]
    } else Map.empty[Coordinates, String]
    val shootingRange = if (includeActiveShootingRange) {
      Map.empty[Coordinates, String]
    } else Map.empty[Coordinates, String]
    val finalState = boardState ++ movementRange ++ shootingRange

    val boardString = new StringBuilder()
    boardString ++= map.HORIZONTAL_BORDER + "\n"
    map.VERTICAL_RANGE.reverse.foreach { y =>
      val row = map.HORIZONTAL_RANGE.map { x =>
        s"|  ${finalState.getOrElse(Coordinates(x, y), " ")}  "
      }.reduce((a, b) => a + b)
      boardString ++= f"$y%4d  " + row + "|\n"
      boardString ++= map.HORIZONTAL_BORDER + "\n"
    }
    boardString ++= "      " + map.HORIZONTAL_RANGE.map(x => f"$x%4d").mkString("  ")

    boardString.toString()
  }

}
