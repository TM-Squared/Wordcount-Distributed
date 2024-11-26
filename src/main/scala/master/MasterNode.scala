package  master

import core._
import java.net.{ServerSocket, Socket}
import scala.collection.mutable
import scala.collection.concurrent.TrieMap
import java.io.PrintWriter

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

    def collectResults(): Map[String, Int] = {
        val globalCounts = TrieMap[String, Int]()

        workers.foreach { socket =>
            val resultData = protocol.receive(socket)
            println(s"Données reçues du Worker : $resultData")
            val result = parseResult(resultData)

            result match {
                case WordCountResult(counts) =>
                    counts.foreach { case (word, count) =>
                        globalCounts.updateWith(word) {
                            case Some(existingCount) => Some(existingCount + count)
                            case None => Some(count)
                        }
                    }
            }
        }

        globalCounts.toMap 
    }


    private def parseResult(data: String): Result = {
        val counts = data.split(",").flatMap { pair =>
            val parts = pair.split(":")
            if (parts.length == 2) {
                val word = parts(0).trim
                val count = parts(1).trim.toIntOption
                count.map(c => word -> c) // Retourne un Some((word, count)) si valide
            } else {
                println(s"Format incorrect ignoré : $pair")
                None // Ignore les paires mal formatées
            }
        }.toMap

        WordCountResult(counts)
    }

    def displayResults(globalCounts: Map[String, Int]): Unit = {
        println("Résultat global du WordCount :")
        globalCounts.foreach { case (word, count) =>
            println(s"$word: $count")
        }
    }



    

    def saveResultsToFile(outputPath: String, globalCounts: Map[String, Int]): Unit = {
        val writer = new PrintWriter(outputPath)
        try {
            globalCounts.foreach { case (word, count) =>
                writer.println(s"$word: $count")
            }
        } finally {
            writer.close()
        }

        println(s"Résultat global écrit dans le fichier : $outputPath")
    }


    override def stop(): Unit = workers.foreach(_.close())
}
