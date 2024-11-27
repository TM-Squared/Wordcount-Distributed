package master

import java.net.Socket
import scala.collection.mutable

class WorkerManager {
    private val workerConnections = mutable.Map[Socket, Boolean]()

    def addWorker(socket: Socket): Unit = {
        workerConnections(socket) = true
    }

    def getAvailableWorker(): Option[Socket] = {
        workerConnections.find(_._2).map { case (socket, _) => 
            workerConnections(socket) = false
            socket
        }
    }

    def releaseWorker(socket: Socket): Unit = {
        workerConnections(socket) = true
    }

    def allWorkers(): Iterable[Socket] = workerConnections.keys

    def removeClosedWorkers(): Unit = {
        workerConnections.keys.foreach { worker =>
            if (worker.isClosed || !worker.isConnected) {
                workerConnections.remove(worker)
                println(s"Worker ${worker.getInetAddress} supprimé (connexion fermée)")
            }
        }
    }
}
