package jobs

import game.{Coordinates, GameUnit}
import models.{Characters, GameCharacter, MapConfig, Maps}

class CheckVictoryConditions {

  def checkVictory (passiveUnit: GameUnit): Option[String] = {
    println("Checking victory conditions...")

    val ALIVE_STATE: String = "alive"

    //    val player1Alive = unit1.state == ALIVE_STATE
    //    val player2Alive = unit2.state == ALIVE_STATE
    //    //    val sExists = map.layout.exists { case (_, value) => value == "S" }
    //    //    val oExists = map.layout.exists { case (_, value) => value == "O" }
    //    //
    //    //    val result = (sExists, oExists) match {


    if (passiveUnit.state == ALIVE_STATE) {
      println("The Battle Rages On")
      Option.empty[String]
    } else {
      passiveUnit.character.avatar match {
        case "S" =>
          println("The Green Tide is Victorious. WAAAAAGGGGH!!!!")
          Option("The Green Tide is Victorious. WAAAAAGGGGH!!!!")

        case "O" =>
          println("The Xenos have been Purged. A Glorious Victory for the Imperium")
          Option("The Xenos have been Purged. A Glorious Victory for the Imperium")

      }
    }
  }
}

