import core.DefaultProtocol
import java.net.{ServerSocket, Socket}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ProtocolSpec extends AnyFlatSpec with Matchers {
  "DefaultProtocol" should "send and receive messages correctly" in {
    val protocol = new DefaultProtocol()
    val server = new ServerSocket(9999)
    val clientThread = new Thread(() => {
      val client = new Socket("localhost", 9999)
      protocol.send(client, "Hello, world!")
      client.close()
    })
    clientThread.start()

    val serverSocket = server.accept()
    val message = protocol.receive(serverSocket)
    message shouldEqual "Hello, world!"
    server.close()
  }
}
