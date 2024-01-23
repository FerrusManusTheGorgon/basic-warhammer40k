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


//  case class Coordinates(x: Int, y: Int)



 val ALIVE_STATE = "alive"
  val DEAD_STATE = "dead"


  val player1Unit = Characters.SpaceMarine
  val player1UnitLocation = Coordinates(3, 4)
  
  val unit1 = GameUnit(
    player1Unit,
    player1UnitLocation,
    ALIVE_STATE
  )

  val player2Unit = Characters.Ork
  val player2UnitLocation = Coordinates(6, 8)
  7,8 5,8 6,7 6,9
  
  

  val unit2 = GameUnit(
    player2Unit,
    player2UnitLocation,
    ALIVE_STATE
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


    
    //map.layout
    map.printMap(unit1, unit2)


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


???

  }

  def printBoard(
                  map: MapConfig,
                  activePlayerUnit: GameUnit,
                  passivePlayerUnit: GameUnit,
                  includeActiveMovementRange: Boolean = false,
                  includeActiveShootingRange: Boolean = false
                ) = {

//    //print top numbers
//    println("" + (0 until verticalLength).map(i => f"$i%4d").mkString("  "))
//    //print barrier
//    println("------+" + ("-----+" * verticalLength))
???
  }
//
//  def printMap(): Unit = {
//    // Print top row numbers
//    println("" + (0 until verticalLength).map(i => f"$i%4d").mkString("  "))
//    println("------+" + ("-----+" * verticalLength))
//    for (i <- 1 until horizontalLength) {
//      print(f"$i%4d  | ")
//      for (j <- 0 until verticalLength) {
//        map(i)(j) match {
//          case Characters.SpaceMarine.avatar => print(s" ${Characters.SpaceMarine.avatar}  | ")
//          case Characters.Ork.avatar => print(s" ${Characters.Ork.avatar}  | ")
//          case "X" => print(" X  | ") // Represents a blocked coordinate
//          case _ => print("    | ")
//        }
//      }
//      println("\n------+" + ("-----+" * verticalLength))
//    }
//
//    // Print the list of coordinates and contents (including blocked coordinates)
//    println("\nCell Contents and Coordinates:")
//    for {
//      i <- 1 until horizontalLength + 1
//      j <- 1 until verticalLength + 1
//    } {
//      println(s"($j, $i): ${map(j + 0)(i + 0)}")
//    }
//
//  }





}
