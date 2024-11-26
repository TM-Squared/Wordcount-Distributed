package core

trait Result {
  def formatted: String
}

case class WordCountResult(counts: Map[String, Int]) extends Result{
    def formatted: String = counts.map{(word, count) => s"$word:$count" }.mkString(",")
}
