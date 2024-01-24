package jobs

import models.{GameCharacter, Characters, Maps, Phases}

class PhaseManagerImpl {
  var currentPhase: Phases = Phases(move = true, rangeAttack = false, closeCombatAttack = false, victoryCheck = false)

  def moveToNextPhase(): Unit = {
    currentPhase = currentPhase match {
      case Phases(move, _, _, _) if move => Phases(move = false, rangeAttack = true, closeCombatAttack = false, victoryCheck = false)
      case Phases(_, rangeAttack, _, _) if rangeAttack => Phases(move = false, rangeAttack = false, closeCombatAttack = true, victoryCheck = false)
      case Phases(_, _, closeCombatAttack, _) if closeCombatAttack => Phases(move = false, rangeAttack = false, closeCombatAttack = false, victoryCheck = true)
      case Phases(_, _, _, victoryCheck) if victoryCheck => Phases(move = true, rangeAttack = false, closeCombatAttack = false, victoryCheck = false)
    }
  }

  def getCurrentPhase(): Phases = currentPhase
}

/*// Usage example
val phaseManager = new PhaseManager()

// Move to the next phase
phaseManager.moveToNextPhase()

// Get the current phase
val currentPhase = phaseManager.getCurrentPhase()*/

