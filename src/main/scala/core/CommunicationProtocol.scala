package core

import java.net.Socket

trait CommunicationProtocol {
    def send(socket: Socket, message: String): Unit
    def receive(socket: Socket): String
}
