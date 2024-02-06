package jobs

import game.{Coordinates, GameUnit}
import models.{Characters, GameCharacter, MapConfig, Maps}
import models.UnitState.ALIVE_STATE
class CheckVictoryConditions {

  def checkVictory(passiveUnits: List[GameUnit]): Option[String] = {
    println("Checking victory conditions...")

    if (passiveUnits.exists(_.state == ALIVE_STATE)) {
      println("The Battle Rages On")
      None
    } else {
      passiveUnits.headOption.flatMap { unit =>
        unit.character.avatar match {
          case "S" =>
            println("The Green Tide is Victorious. WAAAAAGGGGH!!!!")
            Some("The Green Tide is Victorious. WAAAAAGGGGH!!!!")
          case "O" =>
            println("The Xenos have been Purged. A Glorious Victory for the Imperium")
            Some("The Xenos have been Purged. A Glorious Victory for the Imperium")
        }
      }
    }
  }
}


