package master

import core._
import scala.collection.concurrent.TrieMap

class ResultCollector(protocol: CommunicationProtocol, workerManager: WorkerManager) {
    private val globalCounts = TrieMap[String, Int]()

    def collectResults(): Map[String, Int] = {
        workerManager.allWorkers().foreach { worker => 
            val resultData = protocol.receive(worker)
            // peut être ajouté un logger ici
            val result = parseResult(resultData)
            result match {
                case WordCountResult(counts) =>
                    counts.foreach {case (word, count) =>
                        globalCounts.updateWith(word) {
                            case Some(existingCount) => Some(existingCount + count)
                            case None => Some(count)
                        }   
                    }
            
            }
            workerManager.releaseWorker(worker)
        }
        globalCounts.toMap
    }
    

    private def parseResult(data: String): Result = {
        val counts = data.split(",").flatMap { pair =>
            val parts = pair.split(":")
            if (parts.length == 2) {
                val word = parts(0).trim
                val count = parts(1).trim.toIntOption
                count.map(c => word -> c)
            } else None
        }.toMap
        WordCountResult(counts)
    }

}