package core

import java.net.Socket

class DefaultProtocol extends CommunicationProtocol{
    def send(socket: Socket, message: String): Unit = {
        val out = socket.getOutputStream
        out.write((message + "\nEND\n").getBytes())
        out.flush()
    }


    def receive(socket: Socket): String = {
        val in = scala.io.Source.fromInputStream(socket.getInputStream)
        val lines = in.getLines()
        val message = lines.takeWhile(line => line != "END").mkString("\n")
        message
    }
}