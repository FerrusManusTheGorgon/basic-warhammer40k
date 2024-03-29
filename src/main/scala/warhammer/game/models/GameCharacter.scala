package warhammer.game.models

case class GameCharacter(
                          characterId: String,
                          range: Int,
                          ballisticSkill: Int,
                          weaponSkill: Int,
                          movement: Int,
                          avatar: String,
                          name: String,
                          rangedAttackHitMessage: String,
                          rangedAttackMissMessage: String,
                          closeCombatHitMessage: String,
                          closeCombatMissMessage: String,
                          currentPosition: Coordinates,
                          currentStateAlive: Boolean,
                          movePhaseCompleted: Boolean,
                          shootingPhaseCompleted: Boolean,
                          closeCombatPhaseCompleted: Boolean,
                          state: String


                        ) {
  def moved = {
    this.copy(movePhaseCompleted = true)
  }

  def shot = {
    this.copy(shootingPhaseCompleted = true)
  }

  def assaulted = {
    this.copy(closeCombatPhaseCompleted = true)
  }


  def turnCompleted = {
    this.copy(
      movePhaseCompleted = true,
      shootingPhaseCompleted = true,
      closeCombatPhaseCompleted = true
    )
  }
}
