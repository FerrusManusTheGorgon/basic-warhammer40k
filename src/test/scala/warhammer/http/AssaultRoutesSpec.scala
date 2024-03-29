package warhammer.http

import io.undertow.Undertow
import utest._
import warhammer.Main
import warhammer.game.models.{Board, Coordinates}
import warhammer.http.models.ActionRequest
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods.parse
import scalacache.modes.sync.mode
import scalacache.{Cache, sync}
import warhammer.Main.cache
import warhammer.game.{CheckVictoryConditions, CloseCombatManager2Http}

object AssaultRoutesSpec extends TestSuite {
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
    test("checkAssault") - withServer(Main) { host =>
      // Send a request to the /start endpoint to generate and cache a board
      val startData = """{"start": "y"}"""
      val startResponse = requests.post(s"$host/start", data = startData)
      assert(startResponse.statusCode == 200)

      // Retrieve the cached board
      val cachedBoardResponse = sync.get[Board]("123")
      assert(cachedBoardResponse.isDefined)

      // Modify the cached board to set isCloseCombatPhase to true
      val modifiedBoard = cachedBoardResponse.get.copy(isCloseCombatPhase = true)

      // Update the cached board with the modified one
      sync.put("123")(modifiedBoard)

      // Test GET request for assault endpoint
      val getResponse = requests.get(s"$host/assault")
      val expectedSubstring = "No enemies in Assault Range"
      val actualSubstring = getResponse.text().take(70)
      assert(expectedSubstring == actualSubstring)
      assert(getResponse.statusCode == 200)
    }

    test("jassault") - withServer(Main) { host =>
      // Send a request to the /start endpoint to generate and cache a board
      val startData = """{"start": "y"}"""
      val startResponse = requests.post(s"$host/start", data = startData)
      assert(startResponse.statusCode == 200)

      // Test POST request for jassault endpoint
      val postData =
        """
          |{
          |  "avatar": "S",
          |  "x": "3",
          |  "y": "5"
          |}
          |""".stripMargin
      val postResponse = requests.post(s"$host/jassault", data = postData)
      val expectedSubstring = "No enemies in Assault Range"
      val actualSubstring = postResponse.text().take(70)
      assert(expectedSubstring == actualSubstring)
      assert(postResponse.statusCode == 200)
    }
  }
}

