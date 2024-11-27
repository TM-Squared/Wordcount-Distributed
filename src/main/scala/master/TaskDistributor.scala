package master

import core._
import scala.collection.mutable
import java.net.SocketException

class TaskDistributor(protocol: CommunicationProtocol, workerManager: WorkerManager) {
    private val taskQueue = mutable.Queue[Task]()

    def addTasks(tasks: Seq[Task]): Unit = {
        taskQueue.enqueueAll(tasks)
        assignTasksToWorkers()
    }

    private def assignTasksToWorkers(): Unit = {
        new Thread(() => {
            while (taskQueue.nonEmpty || workerManager.getAvailableWorker().isDefined) {
                workerManager.removeClosedWorkers()
                workerManager.getAvailableWorker().foreach { worker =>
                    try {
                        if (!worker.isClosed && taskQueue.nonEmpty) {
                            val task = taskQueue.dequeue()
                            protocol.send(worker, task.data)
                            println(s"Tâche envoyée au Worker : ${worker.getInetAddress}")
                        } else {
                            println(s"Worker ${worker.getInetAddress} n'est pas disponible")
                        }
                    } catch {
                        case ex: SocketException =>
                            println(s"Erreur de socket avec le Worker ${worker.getInetAddress}: ${ex.getMessage}")
                            workerManager.releaseWorker(worker)
                    }
                    
                }
                Thread.sleep(100)
            }
        }).start()
    }
}
