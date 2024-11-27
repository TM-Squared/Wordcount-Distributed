package  master

import core._
import java.net.{ServerSocket, Socket}
import scala.collection.mutable
import scala.collection.concurrent.TrieMap
import java.io.PrintWriter
import java.net.InetSocketAddress

class MasterNode(port: Int, protocol: CommunicationProtocol) extends Node {
    private val workerManager = new WorkerManager()
    private val taskDistributor = new TaskDistributor(protocol, workerManager)
    private val resultCollector = new ResultCollector(protocol, workerManager)
    private var serverSocket: Option[ServerSocket] = None
    private var running: Boolean = true

    override def start(): Unit = {
        try {
            serverSocket = Some(new ServerSocket())
            serverSocket.get.setReuseAddress(true)
            serverSocket.get.bind(new InetSocketAddress(port))
            println(s"MasterNode démarré sur le port $port")

            new Thread(() => {
                while (running && serverSocket.exists(!_.isClosed)){
                    try {
                        val worker = serverSocket.get.accept()
                        workerManager.addWorker(worker)
                        println(s"Worker connecté: ${worker.getInetAddress}")
                    } catch {
                        case e: Exception =>
                            if (running)  {
                                println(s"Erreur lors de l'acceptation d'un Worker : ${e.getMessage}")
                            } else {
                                println("ServerSocket fermé proprement, arrêt du thread d'écoute.")
                            }
                    }
                }
            }).start()
        } catch {
            case e:Exception => 
                println(s"Erreur lors du démarrage du MasterNode : ${e.getMessage}")
        }

    }

    def distributeTasks(tasks:Seq[Task]): Unit = {
        taskDistributor.addTasks(tasks)
    }

    def collectResults(): Map[String, Int] = {
        resultCollector.collectResults()
    }


    def sendStopSignal(): Unit = {
        println("Envoie du signal d'arrêt à tous les workers...")
        workerManager.allWorkers().foreach { worker => 
            if (!worker.isClosed) {
                try {
                    protocol.send(worker, "stop")
                    println(s"Signal d'arrêt envoyé au Worker : ${worker.getInetAddress}")
                } catch {
                    case e: Exception =>
                        println(s"Erreur lors de l'envoi du signal d'arrêt au Worker : ${e.getMessage}")
                }
            }    
        }
        Thread.sleep(4000)
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


    override def stop(): Unit = {
        println("Arrêt du MasterNode")

        running = false
        workerManager.allWorkers().foreach { worker => 
            try {
                if (!worker.isClosed) worker.close()
                println(s"Connexion fermée pour le Worker : ${worker.getInetAddress}")
            } catch {
                case e:Exception => println(s"Erreur lors de la fermeture du serverSocket: ${e.getMessage}")
            }
        }

        serverSocket.foreach { server =>
            try {
                server.close()
                println("ServerSocket fermé.")
            } catch {
                case e: Exception =>
                    println(s"Erreur lors de la fermeture du ServerSocket : ${e.getMessage}")
            }
        }
        println("MasterNode arrêté.")
    }
}
