package  master

import core._
import java.net.{ServerSocket, Socket}
import scala.collection.mutable

class MasterNode(port: Int, protocol: CommunicationProtocol) extends Node {
    private val workers = mutable.ListBuffer[Socket]()
    private val results = mutable.ListBuffer[Result]()

    override def start(): Unit = {
        val serverSocket = new ServerSocket(port)
        println(s"MasterNode started on port $port")

        new Thread(() => {
            while (true){
                val worker = serverSocket.accept()
                workers += worker
            }
        }).start()
    }

    def distributeTasks(tasks: Seq[Task]): Unit = {
        workers.zip(tasks).foreach{ case (worker, task) => 
            protocol.send(worker, task.data)     
        }
    }

    def collectResults(): Seq[Result] = results.toSeq

    override def stop(): Unit = workers.foreach(_.close())
}
