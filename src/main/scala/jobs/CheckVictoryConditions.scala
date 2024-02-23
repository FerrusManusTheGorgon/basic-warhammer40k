package jobs

import game.GameUnit
import models.{Characters, GameCharacter, MapConfig, Maps}
import models.UnitState.ALIVE_STATE
class CheckVictoryConditions {

  def checkVictory(activeUnits: List[GameUnit], passiveUnits: List[GameUnit]): Option[String] = {
    println("Checking victory conditions...")

    //    if (passiveUnits.exists(_.state == ALIVE_STATE)) {
    if (passiveUnits.nonEmpty) {
      println("The Battle Rages On")
      None
    } else {
      activeUnits.headOption.flatMap { unit =>
        unit.character.avatar match {
          case "S" =>
            println("The Xenos have been Purged. A Glorious Victory for the Imperium")
            Some("The Xenos have been Purged. A Glorious Victory for the Imperium")
          case "O" | "9" | "8" | "7" =>
            println("The Green Tide is Victorious. WAAAAAGGGGH!!!!")
            Some("The Green Tide is Victorious. WAAAAAGGGGH!!!!")
        }
      }
    }
  }
}


