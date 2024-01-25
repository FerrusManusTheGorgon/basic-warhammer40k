package jobs

import game.{Coordinates, GameUnit}
import models.{Characters, GameCharacter, MapConfig, Maps}
class CheckVictoryConditions {

  def checkVictory(map: MapConfig): Option[String] = {
    val sExists = map.layout.exists { case (_, value) => value == "S" }
    val oExists = map.layout.exists { case (_, value) => value == "O" }

    (sExists, oExists) match {
      case (true, false) => Some("S wins!")
      case (false, true) => Some("O wins!")
      case _ => None // No winner yet
    }
  }

}
