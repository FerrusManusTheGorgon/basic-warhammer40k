package warhammer

import cask.model.Request
import org.json4s.{DefaultFormats, _}
import org.json4s.jackson.JsonMethods._
import scalacache._
import scalacache.caffeine._
import scalacache.modes.sync._
import warhammer.game.{CheckVictoryConditions, CloseCombatManager2Http, MovementManagerHttp, RangeAttackMangerHttp}
import warhammer.game.models.{Board, Coordinates, GameInitializer, Maps}
import warhammer.http.models.{ActionRequest, StartGameRequest}

import scala.util.Try


object HttpServer2 extends cask.MainRoutes {
  implicit val formats: DefaultFormats.type = DefaultFormats
  implicit val cache: Cache[Board] = CaffeineCache[Board]
  //  val player1Unit: GameCharacter = Characters.SpaceMarine
  //  val player1UnitLocation: Coordinates = Coordinates(3, 4)
  //  val unit1: GameUnit = GameUnit(
  //    character = player1Unit,
  //    coordinates = player1UnitLocation,
  //    state = ALIVE_STATE
  //  )
  //  val player2Unit: GameCharacter = Characters.Ork
  //  val player2UnitLocation: Coordinates = Coordinates(6, 8)
  //  val unit2: GameUnit = GameUnit(
  //    character = player2Unit,
  //    coordinates = player2UnitLocation,
  //    state = ALIVE_STATE
  //  )
  //  val player2BigShootaUnit: GameCharacter = Characters.OrkWithBigShoota
  //  val player2BigShootaUnitLocation: Coordinates = Coordinates(8, 10)
  //  val unit3: GameUnit = GameUnit(
  //    character = player2BigShootaUnit,
  //    coordinates = player2BigShootaUnitLocation,
  //    state = ALIVE_STATE
  //  )
  //  val player2BigChoppaUnit: GameCharacter = Characters.OrkWithBigChoppa
  //  val player2BigChoppaUnitLocation: Coordinates = Coordinates(10, 10)
  //  val unit4: GameUnit = GameUnit(
  //    character = player2BigChoppaUnit,
  //    coordinates = player2BigChoppaUnitLocation,
  //    state = ALIVE_STATE
  //  )
  //  val player2SkorchaUnit: GameCharacter = Characters.OrkWithScorcha
  //  val player2SkorchaUnitLocation: Coordinates = Coordinates(1, 10)
  //  val unit5: GameUnit = GameUnit(
  //    character = player2SkorchaUnit,
  //    coordinates = player2SkorchaUnitLocation,
  //    state = ALIVE_STATE
  //  )
  //
  //
  //  val player1Units: List[GameUnit] = List(unit1)
  //  val player2Units: List[GameUnit] = List(unit2, unit3, unit4, unit5)
  //  val mapWeAreUsing = Maps.RockyDivide
  //  val isPlayer1First = true

  val (player1Units, player2Units) = GameInitializer.initializeGameUnits()
  val mapWeAreUsing = Maps.RockyDivide
  val isPlayer1First = true


  val rangeAttackManager = new RangeAttackMangerHttp
  val closeCombatManager = new CloseCombatManager2Http
  val movementManager = new MovementManagerHttp
  val victoryChecker = new CheckVictoryConditions


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
        isMovePhase = true,
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
      //      Thread.sleep(100) // Add a brief delay
      //
      // Retrieve the cached board
      //      Thread.sleep(100) // Add a brief delay
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

  @cask.post("/jstart/")
  def jStart(request: Request): String = {
    // Parse JSON data from the request body and print it for debugging
    val requestBody = request.text()
    println(s"Request Body: $requestBody")

    // Parse the JSON data and extract the field
    val json = parse(requestBody)
    val startGameRequest = json.extract[StartGameRequest]

    // Perform further processing based on the extracted field
    val userInput = startGameRequest.start.trim.toLowerCase
    if (userInput == "y") {
      // Generate a unique boardId using UUID
      val boardId = "123"
      // Call the method to generate the board string
      val board = Board(
        boardId = boardId,
        player1 = player1Units.map(_.character),
        player2 = player2Units.map(_.character),
        map = mapWeAreUsing,
        isMovePhase = true,
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

      // Return a success message
      s"Board generated with boardId: $boardId and cached.\n${board.printBoard()}"
    } else if (userInput == "n") {
      // Game not started logic here
      "Game not started. Exiting..."
    } else {
      // Invalid input logic here
      "Invalid input. Please enter 'y' or 'n'."
    }
  }


  @cask.post("/move")
  def move(request: Request): String = {
    val boardId = "123" // Assuming the boardId is fixed for now
    // Retrieve the cached board using the boardId
    val cachedBoard: Option[Board] = sync.get(boardId)
    cachedBoard match {
      case Some(board) =>
        if (!board.isMovePhase) {
          // If it's not the move phase, return a message indicating that move actions are not allowed
          s"Move actions are not allowed in the current phase. Current phase: ${getCurrentPhase(board)}"
        } else {
          // Prompt the user to input a curl command with the coordinates
          val userInput = request.text().trim
          val (maybeCoordinates, maybeAvatar) = parseInput(userInput)
          // Check if both coordinates and avatar are parsed successfully
          (maybeCoordinates, maybeAvatar) match {
            case (Some(coordinates), Some(avatar)) =>
              // Call the httpMove method on the movementManager instance
              val (updatedBoard, moveResult) = movementManager.httpMove(coordinates, board, avatar)
              val updatedBoardWithPhase = updatedBoard.phaseManager
              sync.put(boardId)(updatedBoardWithPhase) // Update the cached board
              s"$moveResult\nPhase transition completed. Current phase: ${getCurrentPhase(updatedBoardWithPhase)}"
            case _ =>
              // Return the prompt message if the input format is incorrect
              "Please input a curl command with the coordinates and avatar to move a unit."
          }
        }
      case None =>
        // If the board with the provided boardId is not found, return an error message
        "Board not found. Please provide a valid boardId."
    }
  }


  def getCurrentPhase(board: Board): String = {
    if (board.isMovePhase) "Move"
    else if (board.isShootingPhase) "Shoot"
    else if (board.isCloseCombatPhase) "Assault"
    else "Unknown"
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

  @cask.post("/jmove")
  def jmove(request: Request): String = {
    val boardId = "123" // Assuming the boardId is fixed for now
    // Retrieve the cached board using the boardId
    val cachedBoard: Option[Board] = sync.get(boardId)
    cachedBoard match {
      case Some(board) =>
        if (!board.isMovePhase) {
          // If it's not the move phase, return a message indicating that move actions are not allowed
          s"Move actions are not allowed in the current phase. Current phase: ${getCurrentPhase(board)}"
        } else {
          // Parse JSON data from the request body and print it for debugging
          val requestBody = request.text()
          println(s"Request Body: $requestBody")

          // Parse the JSON data and extract the fields
          val json = parse(requestBody)
          val toCoordinates = (json \ "toCoordinates").extract[Coordinates]
          val avatar = (json \ "avatar").extract[String]

          // Perform further processing using toCoordinates and avatar
          val (updatedBoard, moveResult) = movementManager.httpMove(toCoordinates, board, avatar)
          val updatedBoardWithPhase = updatedBoard.phaseManager
          sync.put(boardId)(updatedBoardWithPhase) // Update the cached board
          s"$moveResult\nPhase transition completed. Current phase: ${getCurrentPhase(updatedBoardWithPhase)}"
        }
      case None =>
        // If the board with the provided boardId is not found, return an error message
        "Board not found. Please provide a valid boardId."
    }
  }


  @cask.get("/shoot/")
  def checkShooter(request: Request): String = {
    val boardId = "123" // Assuming the boardId is fixed for now
    // Retrieve the cached board using the boardId
    val cachedBoard: Option[Board] = sync.get(boardId)
    cachedBoard match {
      case Some(board) =>
        if (!board.isShootingPhase) {
          // If it's not the shoot phase, return a message indicating that shoot actions are not allowed
          s"Shoot actions are not allowed in the current phase. Current phase: ${getCurrentPhase(board)}"
        } else {
          // Your shoot phase logic here
          val activeUnitsAndTargets = rangeAttackManager.getActiveUnitsAndTargets(board)
          val unitsWithRangeTargets = activeUnitsAndTargets.filter { case (_, targets) =>
            targets.nonEmpty
          }

          // Construct the message indicating the units with targets
          val targetsMessage = unitsWithRangeTargets.map { case (unit, targets) =>
            s"${unit.avatar} has targets: ${targets.map(t => s"${t.avatar} at (${t.currentPosition.x}, ${t.currentPosition.y})").mkString(", ")}"
          }.mkString("\n")

          // If there are no active players with targets, return the corresponding message
          if (unitsWithRangeTargets.isEmpty) {
            "No enemies in line of sight"
          }

          // Update shootingPhaseCompleted for units with empty targets
          val unitsWithEmptyRangeTargets = activeUnitsAndTargets.filter { case (_, targets) =>
            targets.isEmpty
          }.keys.toList

          val updatedUnits = unitsWithEmptyRangeTargets.map { unit =>
            unit.copy(shootingPhaseCompleted = true)
          }

          // Update the board with the units that have completed their shooting phase
          val updatedBoardWithCompletedUnits = (board.getActiveDeadPlayers ++ updatedUnits).foldLeft(board) { (currentBoard, updatedUnit) =>
            currentBoard.updateActiveUnit(updatedUnit)
          }

          // Run phaseManager on the updated board
          val updatedBoardWithPhase = updatedBoardWithCompletedUnits.phaseManager

          // Construct the message indicating the phase transition
          val phaseTransitionMessage = s"Phase transition completed. Current phase: ${getCurrentPhase(updatedBoardWithPhase)}"

          // Update the cached board with the phase transition
          sync.put(boardId)(updatedBoardWithPhase)

          // Combine the messages
          val combinedMessage = s"$targetsMessage\n$phaseTransitionMessage"

          // Return the combined message
          combinedMessage
        }
      case None =>
        // If the board with the provided boardId is not found, return an error message
        "Board not found. Please provide a valid boardId."
    }
  }


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
            if (!board.isShootingPhase) {
              // If it's not the shoot phase, return a message indicating that shoot actions are not allowed
              s"Shoot actions are not allowed in the current phase. Current phase: ${getCurrentPhase(board)}"
            } else {
              val (updatedBoard, attackResult) = rangeAttackManager.performRangedAttackHttp(avatar, coordinates, board)
              // Perform phase transition
              val updatedBoardWithPhase = updatedBoard.phaseManager

              sync.put(boardId)(updatedBoardWithPhase) // Update the cached board
              // Print the updated board to local host terminal
              println("Updated board:")
              val victoryMessage = victoryChecker.checkVictory(updatedBoard.getActivePlayers, updatedBoard.getPassivePlayers)

              // Print both attack result, victory message (if available), and phase transition message
              val phaseTransitionMessage = s"Phase transition completed. Current phase: ${getCurrentPhase(updatedBoardWithPhase)}"
              println(s"Attack result: $attackResult")
              victoryMessage.foreach { message =>
                println(s"Victory message: $message")
              }

              println(phaseTransitionMessage)
              // Return the combined message
              s"$attackResult\n$victoryMessage\n$phaseTransitionMessage"
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


  @cask.post("/jshoot/")
  def jshoot(request: Request): String = {
    val json = parse(request.text())
    val actionRequest = json.extract[ActionRequest]
    val boardId = "123" // Assuming the boardId is fixed for now
    val avatar = actionRequest.avatar
    val coordinatesOption = for {
      x <- Try(actionRequest.x.toInt).toOption
      y <- Try(actionRequest.y.toInt).toOption
    } yield Coordinates(x, y)

    val result = coordinatesOption match {
      case Some(coords) =>
        val cachedBoard: Option[Board] = sync.get(boardId)
        cachedBoard match {
          case Some(board) =>
            if (!board.isShootingPhase) {
              // If it's not the shoot phase, return a message indicating that shoot actions are not allowed
              s"Shoot actions are not allowed in the current phase. Current phase: ${getCurrentPhase(board)}"
            } else {
              val (updatedBoard, attackResult) = rangeAttackManager.performRangedAttackHttp(avatar, coords, board)
              // Perform phase transition
              val updatedBoardWithPhase = updatedBoard.phaseManager

              sync.put(boardId)(updatedBoardWithPhase) // Update the cached board
              // Print the updated board to local host terminal
              println("Updated board:")
              val victoryMessage = victoryChecker.checkVictory(updatedBoard.getActivePlayers, updatedBoard.getPassivePlayers)

              // Print both attack result, victory message (if available), and phase transition message
              val phaseTransitionMessage = s"Phase transition completed. Current phase: ${getCurrentPhase(updatedBoardWithPhase)}"
              println(s"Attack result: $attackResult")
              victoryMessage.foreach { message =>
                println(s"Victory message: $message")
              }

              println(phaseTransitionMessage)
              // Return the combined message
              s"$attackResult\n$victoryMessage\n$phaseTransitionMessage"
            }
          case None =>
            "Board not found. Please provide a valid boardId."
        }
      case None =>
        "Invalid input provided. Please provide coordinates and an avatar separated by a space."
    }

    result
  }


  @cask.get("/assault/")
  def assaulty(request: Request): String = {
    val boardId = "123" // Assuming the boardId is fixed for now
    // Retrieve the cached board using the boardId
    val cachedBoard: Option[Board] = sync.get(boardId)
    cachedBoard match {
      case Some(board) =>
        if (!board.isCloseCombatPhase) {
          // If it's not the shoot phase, return a message indicating that shoot actions are not allowed
          s"Assault actions are not allowed in the current phase. Current phase: ${getCurrentPhase(board)}"
        } else {
          // Your assault phase logic here
          val activeUnitsAndAssaultTargets = closeCombatManager.getActiveUnitsAndAssaultTargets(board)
          val unitsWithAssaultTargets = activeUnitsAndAssaultTargets.filter { case (_, targets) =>
            targets.nonEmpty
          }

          // If there are no active players with targets, return the corresponding message
          if (unitsWithAssaultTargets.isEmpty)
            "No enemies in Assault Range"

          // Construct the message indicating the units with targets
          val targetsMessage = unitsWithAssaultTargets.map { case (unit, targets) =>
            s"${unit.avatar} has targets: ${targets.map(t => s"${t.avatar} at (${t.currentPosition.x}, ${t.currentPosition.y})").mkString(", ")}"
          }.mkString("\n")

          // Update shootingPhaseCompleted for units with empty targets
          val unitsWithEmptyAssaultTargets = activeUnitsAndAssaultTargets.filter { case (_, targets) =>
            targets.isEmpty
          }.keys.toList

          val updatedAssaultUnits = unitsWithEmptyAssaultTargets.map { unit =>
            unit.copy(closeCombatPhaseCompleted = true)
          }

          // Update the board with the units that have completed their shooting phase
          val updatedBoardWithCompletedUnits = updatedAssaultUnits.foldLeft(board) { (currentBoard, updatedUnit) =>
            currentBoard.updateActiveUnit(updatedUnit)
          }

          // Run phaseManager on the updated board
          val updatedBoardWithPhase = updatedBoardWithCompletedUnits.phaseManager

          // Construct the message indicating the phase transition
          val phaseTransitionMessage = s"Phase transition completed. Current phase: ${getCurrentPhase(updatedBoardWithPhase)}"

          // Update the cached board with the phase transition
          sync.put(boardId)(updatedBoardWithPhase)

          // Combine the messages
          val combinedMessage = s"$targetsMessage\n$phaseTransitionMessage"

          // Return the combined message
          combinedMessage
        }
      case None =>
        // If the board with the provided boardId is not found, return an error message
        "Board not found. Please provide a valid boardId."
    }
  }


  @cask.post("/assault/")
  def assault(request: Request): String = {
    val userInput = request.text().trim
    val boardId = "123" // Assuming the boardId is fixed for now
    val (assaultCoordinates, avatar) = parseInput2(userInput)

    val result = assaultCoordinates match {
      case Some(coordinates) =>
        val cachedBoard: Option[Board] = sync.get(boardId)
        cachedBoard match {
          case Some(board) =>
            if (!board.isCloseCombatPhase) {
              // If it's not the shoot phase, return a message indicating that shoot actions are not allowed
              s"Assault actions are not allowed in the current phase. Current phase: ${getCurrentPhase(board)}"
            } else {
              val targetedCharacters = closeCombatManager.checkCloseCombatAttackHttp(board)
              val (updatedBoard, attackResult) = closeCombatManager.performCloseCombatAttackHttp(board.map, board.getActivePlayers.head, coordinates, targetedCharacters, board)
              // Perform phase transition

              val updatedBoardWithPhase = updatedBoard.phaseManager
              sync.put(boardId)(updatedBoardWithPhase) // Update the cached board
              val victoryMessage = victoryChecker.checkVictory(updatedBoard.getActivePlayers, updatedBoard.getPassivePlayers)

              val phaseTransitionMessage = s"Phase transition completed. Current phase: ${getCurrentPhase(updatedBoardWithPhase)}"
              println(s"Attack result: $attackResult")
              victoryMessage.foreach { message =>
                println(s"Victory message: $message")
              }

              println(phaseTransitionMessage)
              // Return the combined message
              s"$attackResult\n$victoryMessage\n$phaseTransitionMessage"
            }
          case None =>
            "Board not found. Please provide a valid boardId."
        }
      case None =>
        "Invalid input provided. Please provide coordinates and an avatar separated by a space."
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

  @cask.post("/jassault/")
  def jassault(request: Request): String = {
    val json = parse(request.text())
    val actionRequest = json.extract[ActionRequest]
    val boardId = "123" // Assuming the boardId is fixed for now
    val avatar = actionRequest.avatar
    val coordinatesOption = for {
      x <- Try(actionRequest.x.toInt).toOption
      y <- Try(actionRequest.y.toInt).toOption
    } yield Coordinates(x, y)

    val result = coordinatesOption match {
      case Some(coords) =>
        val cachedBoard: Option[Board] = sync.get(boardId)
        cachedBoard match {
          case Some(board) =>
            if (!board.isCloseCombatPhase) {
              // If it's not the assault phase, return a message indicating that assault actions are not allowed
              s"Assault actions are not allowed in the current phase. Current phase: ${getCurrentPhase(board)}"
            } else {
              // Perform the assault action using the avatar and coordinates
              val targetedCharacters = closeCombatManager.checkCloseCombatAttackHttp(board)
              val (updatedBoard, assaultResult) = closeCombatManager.performCloseCombatAttackHttp(board.map, board.getActivePlayers.head, coords, targetedCharacters, board)
              // Perform phase transition
              val updatedBoardWithPhase = updatedBoard.phaseManager
              sync.put(boardId)(updatedBoardWithPhase) // Update the cached board
              // Check for victory conditions
              val victoryMessage = victoryChecker.checkVictory(updatedBoard.getActivePlayers, updatedBoard.getPassivePlayers)
              // Construct phase transition message
              val phaseTransitionMessage = s"Phase transition completed. Current phase: ${getCurrentPhase(updatedBoardWithPhase)}"
              // Print the result to the console
              println(s"Assault result: $assaultResult")
              victoryMessage.foreach { message =>
                println(s"Victory message: $message")
              }
              println(phaseTransitionMessage)
              // Return the combined message
              s"$assaultResult\n$victoryMessage\n$phaseTransitionMessage"
            }
          case None =>
            "Board not found. Please provide a valid boardId."
        }
      case None =>
        "Invalid input provided. Please provide coordinates and an avatar separated by a space."
    }

    result
  }


  initialize()
}









// curl http://localhost:8080/start/
// curl -X POST http://localhost:8080/start/ -d "y"
// curl -X POST http://localhost:8080/move -d "6 6 S"
//curl http://localhost:8080/shoot/
// curl -X POST http://localhost:8080/move
// curl -X POST -H "Content-Type: application/json" -d '{"boardId":"123", "moveCoordinates":"3 4"}' http://localhost:8080/move
// curl http://localhost:8080/shoot/ -d "6 8 S"
// curl -X POST http://localhost:8080/move -d "6 6 S"
// curl -X POST http://localhost:8080/move -d "6 8 9"
//  curl -X POST http://localhost:8080/move -d "9 6 8"

// curl -X POST -H "Content-Type: application/json" -d '{"start": "y"}' http://localhost:8080/jstart/
//curl -X POST -H "Content-Type: application/json" -d '{"start": "n"}' http://localhost:8080/jstart/
// curl -X POST http://localhost:8080/jshoot/ -H "Content-Type: application/json" -d '{"x": 1, "y": 6, "avatar": "S"}'
// curl -X POST http://localhost:8080/jassault/ -H "Content-Type: application/json" -d '{"x": 6, "y": 6, "avatar": "9"}'
// curl -X POST -H "Content-Type: application/json" -d '{"toCoordinates":{"x":7,"y":6},"avatar":"8"}' http://localhost:8080/jmove
// curl -X POST -H "Content-Type: application/json" -d '{"toCoordinates":{"x":6,"y":8},"avatar":"9"}' http://localhost:8080/jmove
// curl -X POST -H "Content-Type: application/json" -d '{"toCoordinates":{"x":9,"y":6},"avatar":"8"}' http://localhost:8080/jmove
// curl -X POST -H "Content-Type: application/json" -d '{"toCoordinates":{"x":6,"y":6},"avatar":"S"}' http://localhost:8080/jmove
