package worker

import core._
import java.net.Socket

class WorkerNode(masterHost: String, masterPort: Int, protocol: CommunicationProtocol) extends Node {
    override def start(): Unit = {
        val socket = new Socket(masterHost, masterPort)
        println(s"WorkerNode connected to $masterHost:$masterPort")

        new Thread(() => {
            val taskData = protocol.receive(socket)
            val task = WordCountTask(taskData)
            val result = performTask(task)
            protocol.send(socket, result.formatted)
        }).start()
    }

    private def performTask(task: Task): Result = {
        val words = task.data.split("\\s+")
        val counts: Map[String, Int] = words.groupBy(identity).mapValues(_.length).toMap
        WordCountResult(counts)
    }

    override def stop(): Unit = {}
}
