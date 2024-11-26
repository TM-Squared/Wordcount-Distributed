package core

trait Task {
  def data: String
}
case class WordCountTask(data: String) extends Task
