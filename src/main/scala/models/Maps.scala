package models

import game.Coordinates


case object Maps {
  val RockyDivide = MapConfig(
    horizontalLength = 10,
    verticalLength = 10,
    blocker = List(
      Coordinates(6, 5),
      Coordinates(7, 5),
      Coordinates(8, 5),
      Coordinates(9, 5),
      Coordinates(10, 5),
      Coordinates(3, 3),
      Coordinates(2, 3),
      Coordinates(1, 3),
//      Coordinates(15, 6),
      Coordinates(10, 7),
      Coordinates(10, 8),
      Coordinates(10, 9),
      Coordinates(10, 10)
    )
  )
  val spaceMarinePos: Coordinates = Coordinates(3, 4) // Define position for Space Marine within map bounds
  val orkPos: Coordinates = Coordinates(6, 8) // Define position for Ork within map bounds


}
