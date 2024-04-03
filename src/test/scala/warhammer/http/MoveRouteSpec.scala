package warhammer.http

import io.undertow.Undertow
import utest._
import warhammer.Main
import warhammer.game.models.{Board, Coordinates}
import scalacache.modes.sync.mode
import scalacache.{Cache, sync}
import warhammer.Main.cache
object MoveRouteSpec extends TestSuite {
  def withServer[T](example: cask.main.Main)(f: String => T): T = {
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
    test("MinimalApplication2") - withServer(Main) { host =>

      // Send a request to the /start endpoint to generate and cache a board
      val startData = """{"start": "y"}"""
      val startResponse = requests.post(s"$host/start", data = startData)
      assert(startResponse.statusCode == 200)
      val boardId = startResponse.text().slice(30, 66)
      // Retrieve the cached board
      val cachedBoardResponse = sync.get[Board](boardId)
      assert(cachedBoardResponse.isDefined)
      // Validate the response


      // Test POST request for jmove endpoint
      val postData =
        """
          |{
          |  "toCoordinates": {"x": 6, "y": 6},
          |  "avatar": "S"
          |}
          |""".stripMargin


      val postResponse = requests.post(s"$host/move/$boardId", data = postData)
      val expectedSubstring = "-----"
      val actualSubstring = postResponse.text().take(5)
      assert(expectedSubstring == actualSubstring)

      postResponse.statusCode ==> 200
    }
  }
}

