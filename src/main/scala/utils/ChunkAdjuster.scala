package utils

object ChunkAdjuster {
  /**
    * Ajuste dynamiquement le chunkSize en fonction du nombre total de lignes
    * et du nombre de Workers disponibles.
    * 
    * @param totalLines Nombre total de lignes dans le fichier
    * @param numWorkers Nombre de Workers disponibles
    * @param defaultChunkSize ChunkSize par défaut défini dans la configuration
    * @return ChunkSize ajusté
    */
  def adjustChunkSize(totalLines: Int, numWorkers: Int, defaultChunkSize: Int): Int = {
    if (totalLines < defaultChunkSize) {
        Math.max(1, totalLines /  numWorkers)
    } else {
        defaultChunkSize
    }
  }
}
