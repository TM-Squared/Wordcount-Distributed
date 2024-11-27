package client

import core._
import master.MasterNode
import worker.WorkerNode
import utils.{ ChunkAdjuster, LineProcessor }

object ClientApp {
    def main(args: Array[String]): Unit = {
        val masterPort = if (args.length > 0) args(0).toInt else ConfigLoader.getMasterPort
        val dataFile = if (args.length > 1) args(1) else ConfigLoader.getInputFile
        val numWorkers = if (args.length > 2) args(2).toInt else ConfigLoader.getWorkerCount
        val outputFile = if (args.length > 3) args(3) else ConfigLoader.getOutputFile
        val chunkSize = ConfigLoader.getChunkSize
        val maxLineLength = ConfigLoader.getMaxLineLength

        println(s"l'input: $dataFile, l'output: $outputFile, chunkSize: $chunkSize" )
        val protocol = new CompressionProtocol()

        // start master node
        val master = new MasterNode(masterPort, protocol)
        try {
            new Thread(() => master.start()).start()

            // start worker node
            (1 to numWorkers).foreach { _ =>
                new Thread( () => new WorkerNode("localhost", masterPort, protocol).start()).start()    
            }

            // Lecture des données depuis le fichier
            val data = scala.io.Source.fromFile(dataFile).getLines().toSeq
            val totalLines = data.length

            // Traitement des lignes longues
            val processedLines = LineProcessor.splitLongLines(data, maxLineLength)
            println(s"Nombre de lignes après traitement : ${processedLines.length}")


            // Ajustement du chunkSize
            val adjustedChunkSize = ChunkAdjuster.adjustChunkSize(processedLines.length, numWorkers, chunkSize)
            println(s"ChunkSize ajusté : $adjustedChunkSize")

            // Division des données en chunks
            val tasks = processedLines
                .grouped(adjustedChunkSize)
                .map(lines => WordCountTask(lines.mkString("\n")))
                .toSeq
            println(s"${tasks.size} chunks générés.")

            Thread.sleep(2000)

            // Distribute tasks
            master.distributeTasks(tasks)
            println("Task distributed")

            // Collect results
            Thread.sleep(5000)
            var globalCounts = master.collectResults()
            master.saveResultsToFile(outputFile, globalCounts)

            // Envoie du signal d'arrêt
            master.sendStopSignal()
        } catch {
            case ex: Exception =>
                println(s"Erreur lors de l'exécution : ${ex.getMessage}")
                ex.printStackTrace()
        } finally {
            master.stop()
        }

    }
}
