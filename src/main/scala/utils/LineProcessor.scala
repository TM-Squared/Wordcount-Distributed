package utils

object LineProcessor {
    /**
      * Divise une ligne en segments sans couper les mots, en respectant une longueur maximale.
      *
      * @param line la ligne à diviser
      * @param maxLength La longueur maximale de chaque segment
      * @return une séquence de segments
      */
    def splitLineWithoutCuttingWords(line: String, maxLength: Int): Seq[String] = {
        val words = line.split("\\s+") // sépare les mots par les espaces
        val segments = scala.collection.mutable.ListBuffer[String]()
        val currentSegment = new StringBuilder()

        for (word <- words) {
            // Si ajouter le mot dépasse la longueur max, on commence un nouveau segment
            if (currentSegment.length + word.length + 1 > maxLength) {
                segments.append(currentSegment.toString().trim)
                currentSegment.clear()
            }

            // Ajouter le mot au segment courant
            if (currentSegment.nonEmpty) currentSegment.append(" ")
            currentSegment.append(word)
        }

        // Ajouter le dernier segment, s'il existe
        if (currentSegment.nonEmpty) {
        segments.append(currentSegment.toString().trim)
        }

        segments.toSeq
    }

    /**
      * Divise une séquence de lignes en segments plus petits si certaines lignes dépassent une longueur maximale.
      *
      * @param lines Les lignes d'entrée à traiter.
      * @param maxLineLength La longueur maximale d'une ligne avant découpage.
      * @return Une séquence de lignes où aucune ligne ne dépasse maxLineLength.
      */
    def splitLongLines(lines: Seq[String], maxLineLength: Int): Seq[String] = {
        lines.flatMap { line =>
        if (line.length > maxLineLength) {
            // Utilise la méthode sans coupure de mots
            splitLineWithoutCuttingWords(line, maxLineLength)
        } else {
            Seq(line) // Conserve les lignes courtes telles quelles
        }
        }
    }
}
