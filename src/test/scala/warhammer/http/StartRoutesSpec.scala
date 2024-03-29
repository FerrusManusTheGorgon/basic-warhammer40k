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
      //      val success = requests.get(host)

      //      success.text() ==> "Hello World!"
      //      success.statusCode ==> 200


      //      requests.get(s"$host/start", check = false).text() ==> "Would you like to start the game (y/n)?"

      // Test POST request
      // Send a request to the /start endpoint to generate and cache a board
      val startData = """{"start": "y"}"""
      val postResponse = requests.post(s"$host/start", data = startData)

      // Validate the response status code
      assert(postResponse.statusCode == 200)

      // Extract only the first 10 characters of the expected and actual result strings
      val expectedSubstring = "\"Board generated with boardId: 123 and cached.\\n------+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+- - -+".take(70)
      val actualSubstring = postResponse.text().take(70)

      // Validate the first 10 characters of the response text
      assert(expectedSubstring == actualSubstring)
      assert(postResponse.statusCode == 200)
    }
  }
}

  //      requests.get(s"$host/doesnt-exist", check = false).statusCode ==> 404
//
//      requests.post(s"$host/do-thing", data = "hello").text() ==> "olleh"
//
//      requests.delete(s"$host/do-thing", check = false).statusCode ==> 405
    

