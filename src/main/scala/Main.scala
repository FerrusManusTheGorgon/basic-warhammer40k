import game.{Coordinates, GameUnit}
import models.{Characters, GameCharacter, MapConfig, Maps}

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

  val mapWeAreUsing = Maps.RockyDivide
  val isPlayer1First = true

  start(unit1, unit2, mapWeAreUsing, isPlayer1First)

  def start(
             unit1: GameUnit,
             unit2: GameUnit,
             map: MapConfig,
             isPlayer1First: Boolean
           ) : Unit = {

    movement(map, unit1, unit2)

    //map.layout



  }


  def movement(map: MapConfig, activePlayerUnit: GameUnit, passivePlayerUnit: GameUnit): GameUnit = {
    /**
     * print board
     *  We could print valid moves for our user
     * ask for input
     * check if input is valid
     * if valid
     *    update activePlayerUnit
     *    print board
     *    return updated activePlayerUnit
     * if invalid
     *    we want to retry ask for input
     */
//    map.printMap(unit1, unit2)
    printBoard(map, unit1, unit2)

    activePlayerUnit

  }


  def printBoard(
                map: MapConfig,
                activePlayerUnit: GameUnit,
                passivePlayerUnit: GameUnit,
                includeActiveMovementRange: Boolean = false,
                includeActiveShootingRange: Boolean = false
              ): Unit = {
    val currentMap = map.layout + (
      activePlayerUnit.coordinates -> activePlayerUnit.character.avatar,
      passivePlayerUnit.coordinates -> passivePlayerUnit.character.avatar
    )

    val movementRange = if (includeActiveMovementRange) {
      //call method to check range here
      //input unit (need current location, and movement), and currentMap: returns Map[Coordinates, String]
      Map.empty[Coordinates, String]
    } else Map.empty[Coordinates, String]

    val shootingRange = if (includeActiveMovementRange) {
      Map.empty[Coordinates, String]
    } else Map.empty[Coordinates, String]

    val boardState = currentMap
      ++ movementRange
      ++ shootingRange

    println(map.HORIZONTAL_BORDER)

    map.VERTICAL_RANGE.reverse.foreach { y =>
      val row = map.HORIZONTAL_RANGE.map { x =>
        s"|  ${boardState(Coordinates(x, y))}  "
      }.reduce((a, b) => a + b)
      println(f"$y%4d  " + row + "|")
      println(map.HORIZONTAL_BORDER)
    }

    println("      " + map.HORIZONTAL_RANGE.map(x => f"$x%4d").mkString("  "))
  }

}
