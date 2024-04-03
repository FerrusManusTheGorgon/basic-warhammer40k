package warhammer.http

import io.undertow.Undertow
import utest._
import warhammer.Main

object StartRoutesSpec extends TestSuite {
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

      // Test POST request
      // Send a request to the /start endpoint to generate and cache a board
      val startData = """{"start": "y"}"""
      val postResponse = requests.post(s"$host/start", data = startData)

      // Validate the response status code
      assert(postResponse.statusCode == 200)

      // Extract only the first 10 characters of the expected and actual result strings
      val expectedSubstring1 = "Board generated with boardId: "
      val expectedSubstring2 = " and cached."
      val response = postResponse.text()
      val actualSubstring1 = response.take(30)
      val actualSubstring2 = response.slice(66, 78)
      // Validate the first 10 characters of the response text
      assert(expectedSubstring1 == actualSubstring1)
      assert(expectedSubstring2 == actualSubstring2)
    }

  }

}




