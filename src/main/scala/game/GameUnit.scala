package game

import models.{Coordinates, GameCharacter}

case class GameUnit(
                     character: GameCharacter,
                     coordinates: Coordinates,
                     state: String //is this alive or dead
                   )
