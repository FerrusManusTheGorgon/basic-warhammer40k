package warhammer.game.models

case object Maps {
  val RockyDivide = MapConfig(
    horizontalLength = 10,
    verticalLength = 10,
    blocker = List(
      Coordinates(6, 5),
      Coordinates(7, 5),
      Coordinates(9, 5),
      Coordinates(10, 5),
      Coordinates(3, 3),
      Coordinates(2, 3),
      Coordinates(1, 3),
      Coordinates(2, 9),
      Coordinates(3, 9),
      Coordinates(6, 1),
      Coordinates(6, 2),
//      Coordinates(15, 6),
      Coordinates(5, 7),
      Coordinates(5, 8),
      Coordinates(9, 7),
      Coordinates(9, 8),
      Coordinates(9, 9),
      Coordinates(9, 10)
    )
//    spaceMarinePos = List("2,2"),
//    orkPos = List("11,11")
  )
}
