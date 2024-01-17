import jobs.{GameMap, MovementImpl, RangeAttackManager}
import models.{Characters, MapConfig, Maps}


object Main1 extends App {

  // Define initial positions
  val spaceMarinePos = (1, 2)
  val orkPos = (4, 5)

  // Create instances of characters
  val spaceMarine = Characters.SpaceMarine
  val ork = Characters.Ork

  // Create a GameMap instance
  val mapData = Map(
    "HorizontalLength" -> Maps.RockyDivide.horizontalLength,
    "VerticalLength" -> Maps.RockyDivide.verticalLength,
    "Blocker" -> Maps.RockyDivide.blocker.map(coord => {
      val splitCoord = coord.split(",").map(_.toInt)
      (splitCoord(0), splitCoord(1))
    })
  )

  val gameMap = new GameMap(mapData)

  // Create a MapConfig instance based on GameMap data
  val mapConfig: MapConfig = MapConfig(
    horizontalLength = gameMap.horizontalLength,
    verticalLength = gameMap.verticalLength,
    blocker = gameMap.blockedCoordinates.map(coord => s"${coord._1},${coord._2}")
  )

  // Create a MovementImpl instance
  val movement = new MovementImpl(mapConfig)

  // Create a RangeAttackManager instance
  val rangeAttackManager = new RangeAttackManager(isXMove = true, gameMap) // Pass the GameMap instance directly

  // Initialize and print the map
  gameMap.initializeMap(gameMap.spaceMarinePos, gameMap.orkPos, gameMap.spaceMarine, gameMap.ork)
  gameMap.printMap()

  // Print coordinates of Space Marine and Ork
  println(s"Space Marine Coordinates: ${gameMap.spaceMarinePos}")
  println(s"Ork Coordinates: ${gameMap.orkPos}")

  // Call playerMove method
  movement.playerMove()

  // Check and perform ranged attack
  val spaceMarinePosition = gameMap.convertCoordinates(gameMap.spaceMarinePos)
  rangeAttackManager.checkRangedAttack(spaceMarinePosition)
}










