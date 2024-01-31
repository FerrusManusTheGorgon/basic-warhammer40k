package jobs

import game.{Coordinates, GameUnit}
import models.{Characters, GameCharacter, MapConfig, Maps}

class CheckVictoryConditions {

  def checkVictory(unit1: GameUnit, unit2: GameUnit): Option[String] = {
    println("Checking victory conditions...")

    val ALIVE_STATE: String = "alive"

    val player1Alive = unit1.state == ALIVE_STATE
    val player2Alive = unit2.state == ALIVE_STATE
    //    val sExists = map.layout.exists { case (_, value) => value == "S" }
    //    val oExists = map.layout.exists { case (_, value) => value == "O" }
    //
    //    val result = (sExists, oExists) match {


    (player1Alive, player2Alive) match {

      case (true, false) =>
        val message = "The Xenos have been purged. A Glorious victory for the Imperium of Man!"
        println(message)
        Some(message)

      case (false, true) =>
        val message = "The Green Tide is Victorious. WAAAAAGGGGH!!!!"
        println(message)
        Some(message)

      case _ =>
        println("The Battle Rages On. In The Grim Darkness Of The Far Future There Is Only War")
        None // No winner yet
    }
  }

}

