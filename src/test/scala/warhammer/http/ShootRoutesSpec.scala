package warhammer.http

import io.undertow.Undertow
import utest._
import warhammer.Main
import warhammer.game.models.{Board, Coordinates}
import scalacache.modes.sync.mode
import scalacache.{Cache, sync}
import warhammer.Main.cache


object ShootRoutesSpec extends TestSuite {
  def withServer[T](example: cask.main.Main)(f: String => T)(implicit cache: Cache[Board]): T = {
    val server = Undertow.builder
      .addHttpListener(8081, "localhost")
      .setHandler(example.defaultHandler)
      .build
    server.start()
    val res =
      try f("http://localhost:8081")
      finally server.stop()
    res
  }


  val tests = Tests {
    test("checkShooter") - withServer(Main) { host =>
      // Send a request to the /start endpoint to generate and cache a board
      val startData = """{"start": "y"}"""
      val startResponse = requests.post(s"$host/start", data = startData)
      assert(startResponse.statusCode == 200)
      val boardId = startResponse.text().slice(30, 66)
      // Retrieve the cached board
      val cachedBoardResponse = sync.get[Board](boardId)
      assert(cachedBoardResponse.isDefined)

      // Modify the cached board to set isShootingPhase to true
      val modifiedBoard = cachedBoardResponse.get.copy(isShootingPhase = true)

      // Update the cached board with the modified one
      sync.put(boardId)(modifiedBoard)

      // Test GET request for shoot endpoint
      val getResponse = requests.get(s"$host/shoot/$boardId")
      val expectedSubstring = "\nPhase transition completed. Current phase: Move"
      val actualSubstring = getResponse.text().take(70)
      assert(expectedSubstring == actualSubstring)
      assert(getResponse.statusCode == 200)
    }

    test("shoot") - withServer(Main) { host =>
      // Send a request to the /start endpoint to generate and cache a board
      val startData = """{"start": "y"}"""
      val startResponse = requests.post(s"$host/start", data = startData)
      assert(startResponse.statusCode == 200)
      val boardId = startResponse.text().slice(30, 66)
      // Retrieve the cached board
      val cachedBoardResponse = sync.get[Board](boardId)
      assert(cachedBoardResponse.isDefined)

      // Modify the cached board to set isShootingPhase to true
      val cachedBoard = cachedBoardResponse.get

      val modifiedBoard = cachedBoard.copy(
        isShootingPhase = true,
        player1 = cachedBoard.player1.map { character =>
          if (character.avatar == "S") {
            character.copy(ballisticSkill = 100)
          } else {
            character
          }
        },
        player2 = cachedBoard.player2.map { character =>
          if (character.avatar == "O") {
            character.copy(currentPosition = Coordinates(x = 3, y = 6))
          } else {
            character
          }
        }
      )
      // Update the cached board with the modified one
      sync.put(boardId)(modifiedBoard)
      val getResponse = requests.get(s"$host/shoot/$boardId")
      val postData =
        """
          |{
          |  "x": 3,
          |  "y": 6,
          |  "avatar": "S"
          |}
          |""".stripMargin
      val postResponse = requests.post(s"$host/shoot/$boardId", data = postData)
      val expectedSubstring = "Space Marine hits OrkWithSluggaAndChoppa!"
      val actualSubstring = postResponse.text().take(41)
      assert(expectedSubstring == actualSubstring)
      assert(postResponse.statusCode == 200)
    }
  }

}
