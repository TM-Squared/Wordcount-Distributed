package client

import core._
import master.MasterNode
import worker.WorkerNode

object ClientApp {
    def main(args: Array[String]): Unit = {
        if (args.length < 3) {
            println("Usage: ClientApp <masterPort> <dataFile> <numWorkers>")
            System.exit(1)
        }

        val masterPort = args(0).toInt
        val dataFile = args(1)
        val numWorkers = args(2).toInt

        val protocol = new DefaultProtocol()

        // start master node
        val master = new MasterNode(masterPort, protocol)
        new Thread(() => master.start()).start()

        // start worker node
        (1 to numWorkers).foreach { _ =>
            new Thread( () => new WorkerNode("localhost", masterPort, protocol).start()).start()    
        }

        // Read data and split into tasks
        val data = scala.io.Source.fromFile(dataFile).mkString
         val tasks = data.grouped(data.length / numWorkers).map(WordCountTask).toSeq


        // Distribute tasks
        master.distributeTasks(tasks)

        // Collect results
        Thread.sleep(5000)
        val result = master.collectResults()
        result.foreach(r => println(r.formatted))
    }
}
