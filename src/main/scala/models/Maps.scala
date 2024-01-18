package models

case object Maps {
  val RockyDivide = MapConfig(
    horizontalLength = 40,
    verticalLength = 40,
    blocker = List(
      "6,5", "7,5", "8,5", "9,5", "10,5",
      "3,3", "2,3", "1,3", "15,6",
      "10,7", "10,8", "10,9", "10,10"
    )
//    spaceMarinePos = List("2,2"),
//    orkPos = List("11,11")
  )
}
