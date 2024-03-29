package warhammer.http

import io.undertow.Undertow
import utest._
import warhammer.Main

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

      // Validate the response
      assert(startResponse.statusCode == 200)

      // Test POST request for jmove endpoint
      val postData =
        """
          |{
          |  "toCoordinates": {
          |    "x": 6,
          |    "y": 6
          |  },
          |  "avatar": "S"
          |}
          |""".stripMargin

      val postResponse = requests.post(s"$host/jmove", data = postData)
      val expectedSubstring = "Board generated with boardId: 123 and cached."
      val actualSubstring = postResponse.text().take(70)
      assert(expectedSubstring == actualSubstring)

      postResponse.statusCode ==> 200
    }
  }
}

