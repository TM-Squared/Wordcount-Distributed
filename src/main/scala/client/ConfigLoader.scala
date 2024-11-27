package client

import com.typesafe.config.{Config, ConfigFactory}

object ConfigLoader{
    private val config: Config = ConfigFactory.load()

    def getMasterPort: Int = config.getInt("app.master.port")
    def getWorkerCount: Int = config.getInt("app.workers.count")
    def getChunkSize: Int = config.getInt("app.tasks.chunkSize")
    def getInputFile: String = config.getString("app.files.input")
    def getOutputFile: String = config.getString("app.files.output")
    def getMaxLineLength: Int = config.getInt("app.tasks.maxLineLength")
}