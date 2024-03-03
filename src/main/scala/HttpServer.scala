package app

import models.{Board, Characters, Coordinates, GameCharacter, MapConfig, Maps}
import game.GameUnit
import models.UnitState.ALIVE_STATE
import jobs.{CheckVictoryConditions, CloseCombatManager2, CloseCombatManager2Http, GraveYardManager, MapUtils, MovementManager, MovementManagerHttp, RangeAttackManager2, RangeAttackMangerHttp}
import scalacache._
import scalacache.caffeine._
import scalacache.modes.sync._
import scalacache.serialization.binary._
import cask.model.Request

import java.util.UUID
import cask.model.Request
import upickle.default._


object MinimalApplication extends cask.MainRoutes {
  implicit val cache: Cache[Board] = CaffeineCache[Board]
  val player1Unit: GameCharacter = Characters.SpaceMarine
  val player1UnitLocation: Coordinates = Coordinates(3, 4)
  val unit1: GameUnit = GameUnit(
    character = player1Unit,
    coordinates = player1UnitLocation,
    state = ALIVE_STATE
  )
  val player2Unit: GameCharacter = Characters.Ork
  val player2UnitLocation: Coordinates = Coordinates(6, 8)
  val unit2: GameUnit = GameUnit(
    character = player2Unit,
    coordinates = player2UnitLocation,
    state = ALIVE_STATE
  )
  val player2BigShootaUnit: GameCharacter = Characters.OrkWithBigShoota
  val player2BigShootaUnitLocation: Coordinates = Coordinates(8, 10)
  val unit3: GameUnit = GameUnit(
    character = player2BigShootaUnit,
    coordinates = player2BigShootaUnitLocation,
    state = ALIVE_STATE
  )
  val player2BigChoppaUnit: GameCharacter = Characters.OrkWithBigChoppa
  val player2BigChoppaUnitLocation: Coordinates = Coordinates(10, 10)
  val unit4: GameUnit = GameUnit(
    character = player2BigChoppaUnit,
    coordinates = player2BigChoppaUnitLocation,
    state = ALIVE_STATE
  )
  val player2SkorchaUnit: GameCharacter = Characters.OrkWithScorcha
  val player2SkorchaUnitLocation: Coordinates = Coordinates(1, 10)
  val unit5: GameUnit = GameUnit(
    character = player2SkorchaUnit,
    coordinates = player2SkorchaUnitLocation,
    state = ALIVE_STATE
  )


  val player1Units: List[GameUnit] = List(unit1)
  val player2Units: List[GameUnit] = List(unit2, unit3, unit4, unit5)
  val mapWeAreUsing = Maps.RockyDivide
  val isPlayer1First = true


  val george = new RangeAttackManager2
  val rangeAttackManager = new RangeAttackMangerHttp
  val closeCombatManager = new CloseCombatManager2Http
  val graveYardManager = new GraveYardManager
  val movementManager = new MovementManagerHttp
  val victoryChecker = new CheckVictoryConditions

  //  val cache: IO[Cache[IO, String, Board]] = CaffeineCache[IO, String, Board]
  //
  //  implicit val caffeineCache: Cache[IO, String, Board] = cache.unsafeRunSync()


  start()


  @cask.get("/start/")
  def start(): String = {
    "Would you like to start the game (y/n)?"
  }

  @cask.post("/start/")
  def generateBoardString(request: Request): String = {
    val userInput = request.text().trim.toLowerCase
    if (userInput == "y") {
      // Generate a unique boardId using UUID
      //      val boardId = UUID.randomUUID().toString
      val boardId = "123"
      // Call the method to generate the board string
      val board = Board(
        boardId = boardId,
        player1 = player1Units.map(_.character),
        player2 = player2Units.map(_.character),
        map = mapWeAreUsing,
        isMovePhase = false,
        isShootingPhase = false,
        isCloseCombatPhase = false,
        isTopOfTurn = true,
        isPlayer1Turn = true,
        turnNumber = 0
      )
      // Print the board directly
      board.printBoard()
      // Cache the board
      sync.put(boardId)(board)
      Thread.sleep(100) // Add a brief delay
      //
      // Retrieve the cached board
      Thread.sleep(100) // Add a brief delay
      //      val cachedBoard: Option[Board] = get[IO, String, Board](boardId).unsafeRunSync()

      // Return a success message
      s"Board generated with boardId: $boardId and cached.\n${board.printBoard()}"
    } else if (userInput == "n") {
      // Return a message indicating the user opted not to start the game
      "Game not started. Exiting..."
    } else {
      // Return a message indicating invalid input
      "Invalid input. Please enter 'y' or 'n'."
    }
  }

  //  @cask.post("/move")
  //  def move(): String = {
  //    movementManager.httpMove(Coordinates(1, 1), "123")
  //  }

//  @cask.post("/move")
//  def move(request: Request): String = {
//    // Prompt the user to input a curl command with the coordinates
//    val promptMessage = "Please input a curl command with the coordinates to move a unit."
//    val userInput = request.text().trim
//    val boardId = "123" // Assuming the boardId is fixed for now
//    val maybeCoordinates = parseCoordinates(userInput)
//    // Check if the parsed coordinates are valid
//    maybeCoordinates match {
//      case Some(coordinates) =>
//        // Call the httpMove method on the movementManager instance
//        movementManager.httpMove(coordinates, boardId) + s"\nBoard generated with boardId: $boardId and cached."
//      case None =>
//        // Return the prompt message if the input format is incorrect
//        promptMessage
//    }
//  }
//
//  def parseCoordinates(input: String): Option[Coordinates] = {
//    val coordinates = input.split(" ")
//    if (coordinates.length == 2) {
//      try {
//        val x = coordinates(0).toInt
//        val y = coordinates(1).toInt
//        Some(Coordinates(x, y))
//      } catch {
//        case _: NumberFormatException => None // If parsing fails, return None
//      }
//    } else {
//      None // If the input format is incorrect, return None
//    }
//  }
//  @cask.post("/move")
//  def move(request: Request): String = {
//    // Prompt the user to input a curl command with the coordinates
//    val promptMessage = "Please input a curl command with the coordinates and avatar to move a unit."
//    val userInput = request.text().trim
//    val boardId = "123" // Assuming the boardId is fixed for now
//    val (maybeCoordinates, maybeAvatar) = parseInput(userInput)
//    // Check if both coordinates and avatar are parsed successfully
//    (maybeCoordinates, maybeAvatar) match {
//      case (Some(coordinates), Some(avatar)) =>
//        // Call the httpMove method on the movementManager instance
//        movementManager.httpMove(coordinates, boardId, avatar) + s"\nBoard generated with boardId: $boardId and cached."
//      case _ =>
//        // Return the prompt message if the input format is incorrect
//        promptMessage
//    }
//  }
//
//  def parseInput(input: String): (Option[Coordinates], Option[String]) = {
//    val parts = input.split(" ")
//    if (parts.length == 3) {
//      val maybeCoordinates = parseCoordinates(parts(0) + " " + parts(1))
//      val maybeAvatar = Some(parts(2)) // Just pass the third part as the avatar
//      (maybeCoordinates, maybeAvatar)
//    } else {
//      (None, None) // If the input format is incorrect, return None for both coordinates and avatar
//    }
//  }
//  def parseCoordinates(input: String): Option[Coordinates] = {
//        val coordinates = input.split(" ")
//        if (coordinates.length == 2) {
//          try {
//            val x = coordinates(0).toInt
//            val y = coordinates(1).toInt
//            Some(Coordinates(x, y))
//          } catch {
//            case _: NumberFormatException => None // If parsing fails, return None
//          }
//        } else {
//          None // If the input format is incorrect, return None
//        }
//      }
@cask.post("/move")
def move(request: Request): String = {
  // Prompt the user to input a curl command with the coordinates
  val promptMessage = "Please input a curl command with the coordinates and avatar to move a unit."
  val userInput = request.text().trim
  val boardId = "123" // Assuming the boardId is fixed for now
  val (maybeCoordinates, maybeAvatar) = parseInput(userInput)
  // Check if both coordinates and avatar are parsed successfully
  (maybeCoordinates, maybeAvatar) match {
    case (Some(coordinates), Some(avatar)) =>
      // Call the httpMove method on the movementManager instance
      movementManager.httpMove(coordinates, boardId, avatar) + s"\nBoard generated with boardId: $boardId and cached."

    case _ =>
      // Return the prompt message if the input format is incorrect
      promptMessage
  }
}

  def parseCoordinates(input: String): Option[Coordinates] = {
            val coordinates = input.split(" ")
            if (coordinates.length == 2) {
              try {
                val x = coordinates(0).toInt
                val y = coordinates(1).toInt
                Some(Coordinates(x, y))
              } catch {
                case _: NumberFormatException => None // If parsing fails, return None
              }
            } else {
              None // If the input format is incorrect, return None
            }
          }

  def parseInput(input: String): (Option[Coordinates], Option[String]) = {
    val parts = input.split(" ")
    if (parts.length == 3) {
      if (parts(0).equalsIgnoreCase("Hold") && parts(1).equalsIgnoreCase("Ground")) {
        // Special case for "Hold Ground avatar"
        (Some(Coordinates(100, 100)), Some(parts(2)))
      } else {
        val maybeCoordinates = parseCoordinates(parts(0) + " " + parts(1))
        val maybeAvatar = Some(parts(2)) // Just pass the third part as the avatar
        (maybeCoordinates, maybeAvatar)
      }
    } else {
      (None, None) // If the input format is incorrect, return None for both coordinates and avatar
    }
  }

//  @cask.get("/shoot/")
//  def checkShoot(request: Request): String = {
//    val boardId = "123" // Assuming the boardId is fixed for now
//    // Retrieve the cached board using the boardId
//    val cachedBoard: Option[Board] = sync.get(boardId)
//    cachedBoard match {
//      case Some(board) =>
//        // If the board is found, call the method to check ranged attack
//        val targetedCharacters = rangeAttackManager.checkRangedAttackHttp(board)
//        // Build a string to display the list of targeted characters
//        val result = new StringBuilder
//        result.append("List of targeted characters:\n")
//        targetedCharacters.foreach { character =>
//          result.append(s"${character.avatar} in range at (${character.currentPosition.x}, ${character.currentPosition.y})\n")
//        }
//        // Convert the StringBuilder to a string and return it
//        result.toString()
//      case None =>
//        // If the board with the provided boardId is not found, return an error message
//        "Board not found. Please provide a valid boardId."
//    }
//  }
@cask.get("/shoot/")
def checkShoot(request: Request): String = {
  val boardId = "123" // Assuming the boardId is fixed for now
  // Retrieve the cached board using the boardId
  val cachedBoard: Option[Board] = sync.get(boardId)
  cachedBoard match {
    case Some(board) =>
      // If the board is found, call the method to check ranged attack
      val targetedCharacters = rangeAttackManager.checkRangedAttackHttp(board)
      // Build a string to display the list of targeted characters
      val result = new StringBuilder
      result.append("List of targeted characters:\n")
      targetedCharacters.foreach { character =>
        result.append(s"${character.avatar} in range at (${character.currentPosition.x}, ${character.currentPosition.y})\n")
      }

      // Call the method to check for active units with no line of sight
      val updatedActiveUnits = rangeAttackManager.activeUnitsWithNoLineOfSight(board.getActivePlayers, board.map, board.getPassivePlayers)
      // Append the result of updated active units to the response string
      result.append("Updated active units:\n")
      updatedActiveUnits.foreach { unit =>
        result.append(s"${unit.avatar} shootingPhaseCompleted: ${unit.shootingPhaseCompleted}\n")
      }

      // Convert the StringBuilder to a string and return it
      result.toString()

    case None =>
      // If the board with the provided boardId is not found, return an error message
      "Board not found. Please provide a valid boardId."
  }
}


  //  @cask.post("/shoot/")
//  def Shoot(request: Request): String = {
//    val userInput = request.text().trim
//    val boardId = "123" // Assuming the boardId is fixed for now
//    val shootCoordinates = parseCoordinates2(userInput)
//
//    val result = shootCoordinates match {
//      case Some(coordinates) =>
//        // If the board is found, call the method to check ranged attack
////        val shotCharacter = rangeAttackManager.rangeAttackHttpIfInRange(coordinates, boardId)
//        // Build a string to display the targeted character
//        shootCoordinates match {
//          case Some(character) =>
//            rangeAttackManager.rangeAttackHttpIfInRange(coordinates, boardId)
//        }
//      case None =>
//        // If the coordinates are not provided or invalid, return an error message
//        "Invalid coordinates provided. Please provide two integers separated by a space."
//    }
//
//    result
//  }
//
//  def parseCoordinates2(input: String): Option[Coordinates] = {
//    val coordinates = input.split(" ")
//    if (coordinates.length == 2) {
//      try {
//        val x = coordinates(0).toInt
//        val y = coordinates(1).toInt
//        Some(Coordinates(x, y))
//      } catch {
//        case _: NumberFormatException => None // If parsing fails, return None
//      }
//    } else {
//      None // If the input format is incorrect, return None
//    }
//  }

//  @cask.post("/shoot/")
//  def Shoot(request: Request): String = {
//    val userInput = request.text().trim
//    val boardId = "123" // Assuming the boardId is fixed for now
//    val shootCoordinates = parseCoordinates2(userInput)
//
//    val result = shootCoordinates match {
//      case Some(coordinates) =>
//        // If the board is found, call the method to check ranged attack
//        val cachedBoard: Option[Board] = sync.get(boardId)
//        cachedBoard match {
//          case Some(board) =>
//            val targetedCharacters = rangeAttackManager.checkRangedAttackHttp(board) // Capture targetedCoordinates
//            println(targetedCharacters)
//            rangeAttackManager.rangeAttackHttpIfInRange(coordinates, boardId) match {
//              case "Ranged attack performed successfully." =>
//                // If ranged attack was performed successfully, execute performRangedAttack
//                rangeAttackManager.performRangedAttackHttp(board.map, board.getActivePlayers.head, coordinates, targetedCharacters)
//                "Ranged attack executed."
//                board.printBoard()
//              case message =>
//                message // Return the message from rangeAttackHttpIfInRange
//            }
//          case None =>
//            "Board not found. Please provide a valid boardId."
//        }
//      case None =>
//        // If the coordinates are not provided or invalid, return an error message
//        "Invalid coordinates provided. Please provide two integers separated by a space."
//    }
//
//    result
//  }

//  @cask.post("/shoot/")
//  def shoot(request: Request): String = {
//    val userInput = request.text().trim
//    val boardId = "123" // Assuming the boardId is fixed for now
//    val (shootCoordinates, avatar) = parseInput2(userInput)
//
//    val result = shootCoordinates match {
//      case Some(coordinates) =>
//        val cachedBoard: Option[Board] = sync.get(boardId)
//        cachedBoard match {
//          case Some(board) =>
//            val targetedCharacters = rangeAttackManager.checkRangedAttackHttp(board)
//            val attackResult = rangeAttackManager.rangeAttackHttpIfInRange(coordinates, boardId, avatar)
//            attackResult match {
//              case "Ranged attack performed successfully." =>
//                val hitOrMissMessage = rangeAttackManager.performRangedAttackHttp(board.map, board.getActivePlayers.head, coordinates, targetedCharacters, board)
//                // Print hit or miss message to local host terminal
//                println(hitOrMissMessage)
//                // Print the updated board to local host terminal
//                println("Updated board:")
//                board.printBoard()
//                hitOrMissMessage
//              case message =>
//                message
//            }
//          case None =>
//            "Board not found. Please provide a valid boardId."
//        }
//      case None =>
//        "Invalid input provided. Please provide coordinates and an avatar separated by a space."
//    }
//
//    result
//  }
//
//  def parseInput2(input: String): (Option[Coordinates], String) = {
//    val parts = input.split(" ")
//    if (parts.length == 3) {
//      try {
//        val x = parts(0).toInt
//        val y = parts(1).toInt
//        val avatar = parts(2)
//        (Some(Coordinates(x, y)), avatar)
//      } catch {
//        case _: NumberFormatException => (None, "") // If parsing fails, return None
//      }
//    } else {
//      (None, "") // If the input format is incorrect, return None
//    }
//  }
//
//  def parseCoordinates2(input: String): Option[Coordinates] = {
//    val coordinates = input.split(" ")
//    if (coordinates.length == 2) {
//      try {
//        val x = coordinates(0).toInt
//        val y = coordinates(1).toInt
//        Some(Coordinates(x, y))
//      } catch {
//        case _: NumberFormatException => None // If parsing fails, return None
//      }
//    } else {
//      None // If the input format is incorrect, return None
//    }
//  }

  @cask.post("/shoot/")
  def shoot(request: Request): String = {
    val userInput = request.text().trim
    val boardId = "123" // Assuming the boardId is fixed for now
    val (shootCoordinates, avatar) = parseInput2(userInput)

    val result = shootCoordinates match {
      case Some(coordinates) =>
        val cachedBoard: Option[Board] = sync.get(boardId)
        cachedBoard match {
          case Some(board) =>
            val targetedCharacters = rangeAttackManager.checkRangedAttackHttp(board)
            val attackResult = rangeAttackManager.rangeAttackHttpIfInRange(coordinates, boardId, avatar)
            attackResult match {
              case "Ranged attack performed successfully." =>
                val hitOrMissMessage = rangeAttackManager.performRangedAttackHttp(board.map, board.getActivePlayers.head, coordinates, targetedCharacters, board)
                // Print hit or miss message to local host terminal
                println(hitOrMissMessage)
                // Print the updated board to local host terminal
                println("Updated board:")
                board.printBoard()
                hitOrMissMessage
              case message =>
                message
            }
          case None =>
            "Board not found. Please provide a valid boardId."
        }
      case None =>
        "Invalid input provided. Please provide coordinates and an avatar separated by a space."
    }

    result
  }

  def parseCoordinates2(input: String): Option[Coordinates] = {
        val coordinates = input.split(" ")
        if (coordinates.length == 2) {
          try {
            val x = coordinates(0).toInt
            val y = coordinates(1).toInt
            Some(Coordinates(x, y))
          } catch {
            case _: NumberFormatException => None // If parsing fails, return None
          }
        } else {
          None // If the input format is incorrect, return None
        }
      }

  def parseInput2(input: String): (Option[Coordinates], String) = {
    val parts = input.split(" ")
    if (parts.length == 3) {
      if (parts(0).equalsIgnoreCase("Hold") && parts(1).equalsIgnoreCase("Fire")) {
        // Special case for "Hold Fire Avatar"
        (Some(Coordinates(100, 100)), parts(2))
      } else {
        try {
          val x = parts(0).toInt
          val y = parts(1).toInt
          val avatar = parts(2)
          (Some(Coordinates(x, y)), avatar)
        } catch {
          case _: NumberFormatException => (None, "") // If parsing fails, return None
        }
      }
    } else {
      (None, "") // If the input format is incorrect, return None
    }
  }


  @cask.get("/assault/")
  def checkAssault(request: Request): String = {
    val boardId = "123" // Assuming the boardId is fixed for now
    // Retrieve the cached board using the boardId
    val cachedBoard: Option[Board] = sync.get(boardId)
    cachedBoard match {
      case Some(board) =>
        // If the board is found, call the method to check ranged attack
        val targetedCharacters = closeCombatManager.checkCloseCombatAttackHttp(board)
        // Build a string to display the list of targeted characters
        val result = new StringBuilder
        result.append("List of targeted characters:\n")
        targetedCharacters.foreach { character =>
          result.append(s"${character.avatar} in assault range at (${character.currentPosition.x}, ${character.currentPosition.y})\n")
        }
        // Call the method to check for active units with no line of sight
        val updatedActiveUnits = closeCombatManager.activeUnitsNotInAssaultRange(board.getActivePlayers, board.map, board.getPassivePlayers)
        // Append the result of updated active units to the response string
        result.append("Updated active units:\n")
        updatedActiveUnits.foreach { unit =>
          result.append(s"${unit.avatar} closeCombatPhaseCompleted: ${unit.closeCombatPhaseCompleted}\n")
        }
        // Convert the StringBuilder to a string and return it
        result.toString()
      case None =>
        // If the board with the provided boardId is not found, return an error message
        "Board not found. Please provide a valid boardId."
    }
  }



//  @cask.post("/assault/")
//  def assault(request: Request): String = {
//    val userInput = request.text().trim
//    val boardId = "123" // Assuming the boardId is fixed for now
//    val (assaultCoordinates, avatar) = parseInput3(userInput)
//    val result = assaultCoordinates match {
//      case Some(coordinates) =>
//        val cachedBoard: Option[Board] = sync.get(boardId)
//        cachedBoard match {
//          case Some(board) =>
//            val targetedCharacters = closeCombatManager.checkCloseCombatAttackHttp(board)
//            val attackResult = closeCombatManager.performCloseCombatAttackHttpIfInRange(coordinates, boardId, avatar)
//            attackResult match {
//              case "Close Combat attack performed successfully." =>
//                val hitOrMissMessage = closeCombatManager.performCloseCombatAttackHttp(board.map, board.getActivePlayers.head, coordinates, targetedCharacters, board)
////                val hitOrMissMessage = rangeAttackManager.performRangedAttackHttp(board.map, board.getActivePlayers.head, coordinates, targetedCharacters, board)
//
//                // Print hit or miss message to local host terminal
//                println(hitOrMissMessage)
//                // Print the updated board to local host terminal
//                println("Updated board:")
//                board.printBoard()
//                hitOrMissMessage
//              case message =>
//                message
//            }
//          case None =>
//            "Board not found. Please provide a valid boardId."
//        }
//      case None =>
//        "Invalid coordinates provided. Please provide two integers separated by a space."
//    }
//
//    result
//  }
//
//  def parseInput3(input: String): (Option[Coordinates], String) = {
//    val parts = input.split(" ")
//    if (parts.length == 3) {
//      try {
//        val x = parts(0).toInt
//        val y = parts(1).toInt
//        val avatar = parts(2)
//        (Some(Coordinates(x, y)), avatar)
//      } catch {
//        case _: NumberFormatException => (None, "") // If parsing fails, return None
//      }
//    } else {
//      (None, "") // If the input format is incorrect, return None
//    }
//  }
@cask.post("/assault/")
def assault(request: Request): String = {
  val userInput = request.text().trim
  val boardId = "123" // Assuming the boardId is fixed for now
  val (assaultCoordinates, avatar) = parseInput3(userInput)
  val result = assaultCoordinates match {
    case Some(coordinates) =>
      val cachedBoard: Option[Board] = sync.get(boardId)
      cachedBoard match {
        case Some(board) =>
          val targetedCharacters = closeCombatManager.checkCloseCombatAttackHttp(board)
          val attackResult = closeCombatManager.performCloseCombatAttackHttpIfInRange(coordinates, boardId, avatar)
          attackResult match {
            case "Close Combat attack performed successfully." =>
              val hitOrMissMessage = closeCombatManager.performCloseCombatAttackHttp(board.map, board.getActivePlayers.head, coordinates, targetedCharacters, board)

              // Print hit or miss message to local host terminal
              println(hitOrMissMessage)
              // Print the updated board to local host terminal
              println("Updated board:")
              board.printBoard()
              hitOrMissMessage
            case message =>
              message
          }
        case None =>
          "Board not found. Please provide a valid boardId."
      }
    case None =>
      "Invalid coordinates provided. Please provide two integers separated by a space."
  }

  result
}

  def parseInput3(input: String): (Option[Coordinates], String) = {
    val parts = input.split(" ")
    if (parts.length == 3) {
      if (parts(0).equalsIgnoreCase("Hold") && parts(1).equalsIgnoreCase("Attack")) {
        // Special case for "Hold Attack Avatar"
        (Some(Coordinates(100, 100)), parts(2))
      } else {
        try {
          val x = parts(0).toInt
          val y = parts(1).toInt
          val avatar = parts(2)
          (Some(Coordinates(x, y)), avatar)
        } catch {
          case _: NumberFormatException => (None, "") // If parsing fails, return None
        }
      }
    } else {
      (None, "") // If the input format is incorrect, return None
    }
  }


  def parseCoordinates3(input: String): Option[Coordinates] = {
    val coordinates = input.split(" ")
    if (coordinates.length == 2) {
      try {
        val x = coordinates(0).toInt
        val y = coordinates(1).toInt
        Some(Coordinates(x, y))
      } catch {
        case _: NumberFormatException => None // If parsing fails, return None
      }
    } else {
      None // If the input format is incorrect, return None
    }
  }

  //  @cask.post("/shoot")
//  def shoot(request: Request): String = {
//    // Prompt the user to input a curl command with the coordinates
//    val promptMessage = "Please input a curl command with the coordinates to shoot a unit."
//    val userInput = request.text().trim
//    val boardId = "123" // Assuming the boardId is fixed for now
//    val maybeCoordinates = parseCoordinates(userInput)
//  }

//    def parseCoordinates(input: String): Option[Coordinates] = {
//      val coordinates = input.split(" ")
//      if (coordinates.length == 2) {
//        try {
//          val x = coordinates(0).toInt
//          val y = coordinates(1).toInt
//          Some(Coordinates(x, y))
//        } catch {
//          case _: NumberFormatException => None // If parsing fails, return None
//        }
//      } else {
//        None // If the input format is incorrect, return None
//
//      }
//    }

    initialize()
  }









// curl http://localhost:8080/start/
// curl -X POST http://localhost:8080/start/ -d "y"
// curl -X POST http://localhost:8080/move
// curl -X POST -H "Content-Type: application/json" -d '{"boardId":"123", "moveCoordinates":"3 4"}' http://localhost:8080/move