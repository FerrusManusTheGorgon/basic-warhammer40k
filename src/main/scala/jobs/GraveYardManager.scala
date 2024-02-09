package jobs

import game.{Coordinates, GameUnit}
import models.UnitState.{ALIVE_STATE, DEAD_STATE}
import models.{Characters, GameCharacter, MapConfig, Maps}

class GraveYardManager {

  def removeDeadCharacters(passiveUnits: List[GameUnit]): List[GameUnit] = {
    // Filter out the alive units
    val aliveUnits = passiveUnits.filter(_.state == ALIVE_STATE)

    // Print the list of alive units
    println("Alive Units:")
    aliveUnits.foreach(unit => println(s"Unit: ${unit.character.name}, Coordinates: (${unit.coordinates.x}, ${unit.coordinates.y})"))

    // Return the list of alive units
    aliveUnits
  }


}



