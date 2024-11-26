package core

import java.net.{Socket, ServerSocket}

trait CommunicationProtocol {
    def send(socket: Socket, message: String): Unit
    def receive(socket: Socket): String
}

class DefaultProtocol extends CommunicationProtocol{
    def send(socket: Socket, message: String): Unit = {
        val out = socket.getOutputStream
        out.write((message + "\n").getBytes())
        out.flush()
    }

    def receive(socket: Socket): String = {
        val in = socket.getInputStream
        scala.io.Source.fromInputStream(in).getLines().mkString("\n")
    }
}
