package jobs

import game.{Coordinates, GameUnit}
import models.UnitState.DEAD_STATE
import models.{Characters, GameCharacter, MapConfig, Maps}

class GraveYardManager {

  def removeDeadCharacters(boardState: Map[Coordinates, String]): Map[Coordinates, String] = {
    boardState.filter { case (_, state) => state != DEAD_STATE }
  }
}



