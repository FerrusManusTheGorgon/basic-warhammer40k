package models

import models.{Characters, GameCharacter}


case class Board(
                  boardId: String,
                  player1: List[GameCharacter],
                  player2: List[GameCharacter],
                  map: MapConfig,
                  isMovePhase: Boolean,
                  isShootingPhase: Boolean,
                  isCloseCombatPhase: Boolean,
                  isTopOfTurn: Boolean,
                  isPlayer1Turn: Boolean,
                  turnNumber: Int
                ) {

  def updatePlayer1Unit(updatedCharacter: GameCharacter): List[GameCharacter] = {
    this.player1.filterNot(_.characterId == updatedCharacter.characterId) :+ updatedCharacter
  }

  def updatePlayer2Unit(updatedCharacter: GameCharacter): List[GameCharacter] = {
    this.player2.filterNot(_.characterId == updatedCharacter.characterId) :+ updatedCharacter
  }
  
  def getPassivePlayers : List[GameCharacter]= {
    if(this.isPlayer1Turn){
      this.player2
    }else{
      this.player1
    }
  }
  
  def print: String = {
    
    "XXXXXX"
  }
}



