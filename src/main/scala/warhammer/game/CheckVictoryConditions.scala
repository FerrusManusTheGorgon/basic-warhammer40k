package warhammer.game

import warhammer.game.models.GameCharacter

class CheckVictoryConditions {

  def checkVictory(activeUnits: List[GameCharacter], passiveUnits: List[GameCharacter]): String = {
    println("Checking victory conditions...")
    println("Active units:")
    activeUnits.foreach(println)

    println("Passive units:")
    passiveUnits.foreach(println)

    if (passiveUnits.exists(x => x.state == "alive")) {
      println("The Battle Rages On")
      "The Battle Rages On"
    } else {
      activeUnits.headOption match {
        case Some(unit) =>
          unit.avatar match {
            case "S" =>
              println("The Xenos have been Purged. A Glorious Victory for the Imperium")
              "The Xenos have been Purged. A Glorious Victory for the Imperium"
            case "O" | "9" | "8" | "7" =>
              println("The Green Tide is Victorious. WAAAAAGGGGH!!!!")
              "The Green Tide is Victorious. WAAAAAGGGGH!!!!"
          }
        case None =>
          println("No units left on the battlefield.")
          "No units left on the battlefield."
      }
    }
  }

}



