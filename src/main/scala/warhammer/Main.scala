package warhammer

import warhammer.http.{AssaultRoutes, MoveRoute, ShootRoutes, StartRoutes}
import scalacache.Cache
import scalacache.caffeine.CaffeineCache
import warhammer.game.{CheckVictoryConditions, CloseCombatManager, MovementManager, RangeAttackManger}
import warhammer.game.models.Board

import java.util.UUID

object Main extends cask.Main {
  println("starting warhammer server")

  override def host: String = "0.0.0.0"
  implicit val cache: Cache[Board] = CaffeineCache[Board]

  val rangeAttackManager = new RangeAttackManger
  val closeCombatManager = new CloseCombatManager
  val movementManager = new MovementManager
  val victoryChecker = new CheckVictoryConditions
  // Generate a unique boardId using UUID
//  val boardId = UUID.randomUUID().toString
//  val startRoutes = StartRoutes(boardId)


  val allRoutes = Seq(
    StartRoutes(),
    MoveRoute(movementManager),
    ShootRoutes(rangeAttackManager, victoryChecker),
    AssaultRoutes(closeCombatManager, victoryChecker)
  )
}
