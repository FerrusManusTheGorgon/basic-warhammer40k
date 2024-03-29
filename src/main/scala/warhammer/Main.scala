package warhammer

import warhammer.http.{StartRoutes, ShootRoutes, MoveRoute, AssaultRoutes}
import scalacache.Cache
import scalacache.caffeine.CaffeineCache
import warhammer.game.{CheckVictoryConditions, CloseCombatManager2Http, MovementManagerHttp, RangeAttackMangerHttp}
import warhammer.game.models.Board

object Main extends cask.Main {
  println("starting warhammer server")

  override def host: String = "0.0.0.0"
  implicit val cache: Cache[Board] = CaffeineCache[Board]

  val rangeAttackManager = new RangeAttackMangerHttp
  val closeCombatManager = new CloseCombatManager2Http
  val movementManager = new MovementManagerHttp
  val victoryChecker = new CheckVictoryConditions

  val allRoutes = Seq(
    StartRoutes(),
    MoveRoute(movementManager),
    ShootRoutes(rangeAttackManager, victoryChecker),
    AssaultRoutes(closeCombatManager, victoryChecker)
  )
}
