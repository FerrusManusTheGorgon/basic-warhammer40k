package game

case class ArmyList(
                     hq: List[ArmyUnit],
                     elites: List[ArmyUnit],
                     troops: List[ArmyUnit],
                     fastAttack: List[ArmyUnit],
                     heavySupport: List[ArmyUnit]
                   ) {
  def isLegalArmy: Boolean = {
    true
  }

}
