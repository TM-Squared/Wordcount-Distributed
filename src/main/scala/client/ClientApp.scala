package client

import core._
import master.MasterNode
import worker.WorkerNode

object ClientApp {
    def main(args: Array[String]): Unit = {
        if (args.length < 4) {
            println("Usage: ClientApp <masterPort> <dataFile> <numWorkers> <outputFile>")
            System.exit(1)
        }

        val masterPort = args(0).toInt
        val dataFile = args(1)
        val numWorkers = args(2).toInt
        val outputFile = args(3)

        val protocol = new CompressionProtocol()

        // start master node
        val master = new MasterNode(masterPort, protocol)
        new Thread(() => master.start()).start()

        // start worker node
        (1 to numWorkers).foreach { _ =>
            new Thread( () => new WorkerNode("localhost", masterPort, protocol).start()).start()    
        }

        // Read data and split into tasks
        val chunkSize = 1000 // Nombre de ligne par bloc
        val data = scala.io.Source.fromFile(dataFile).mkString
        val tasks = data.grouped(data.length / chunkSize).map(lines => WordCountTask(lines.mkString("\n"))).toSeq

        Thread.sleep(2000)
        // Distribute tasks
        master.distributeTasks(tasks)
        println("Task distributed")

        // Collect results
        Thread.sleep(5000)
        var globalCounts = master.collectResults()
        master.saveResultsToFile(outputFile, globalCounts)

    }
}
