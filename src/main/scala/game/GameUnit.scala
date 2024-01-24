package game

import models.GameCharacter

case class GameUnit(
                     character: GameCharacter,
                     coordinates: Coordinates,
                     state: String //is this alive or dead
                   )
