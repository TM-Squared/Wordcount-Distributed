package worker

import core._
import java.net.Socket
import java.net.SocketException

class WorkerNode(masterHost: String, masterPort: Int, protocol: CommunicationProtocol) extends Node {
    private val socket = new Socket(masterHost, masterPort)
    override def start(): Unit = {
        
        println(s"WorkerNode connected to $masterHost:$masterPort")
        try {
            while (!socket.isClosed) {
                val taskData = protocol.receive(socket)
                println(s"Tâche reçue : $taskData")

                // Si le Master envoie une indication de fin, arrêter
                if (taskData.trim.toLowerCase == "stop") {
                    println("Signal d'arrêt reçu du Master. Arrêt du Worker.")
                    stop()
                    return
                }

                val task = WordCountTask(taskData)
                val result = performTask(task)
                protocol.send(socket, result.formatted)
                println(s"Résultat envoyé au Master : ${result.formatted}")
            }
        } catch {
            case ex: SocketException =>
                println(s"Connexion au Master perdue : ${ex.getMessage}")
            case ex: Exception => 
                println(s"Erreur inattendue : ${ex.getMessage}")
                ex.printStackTrace()
        } finally {
            stop()
        }
    }

    private def performTask(task: Task): Result = {
        val words = task.data.split("\\s+")
        val counts: Map[String, Int] = words.groupBy(identity).map { case(word, occurrences) => word -> occurrences.length}
        WordCountResult(counts)
    }

    override def stop(): Unit = {
        if(!socket.isClosed) {
            println(s"Fermeture de la connexion pour le Worker ${socket.getInetAddress}")
            socket.close()
        }
    }
}
