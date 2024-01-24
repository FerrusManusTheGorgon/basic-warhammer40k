package game

trait Phase {
//  def start: Board 
  
  def nextPhase: Option[Phase]

}
