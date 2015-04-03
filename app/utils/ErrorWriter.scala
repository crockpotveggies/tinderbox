package utils

/**
 * takes a Throwable and writes it to a string
 */
object ErrorWriter {
  def writeString(e: Throwable): String = {
    val writer = new java.io.StringWriter()
    val printWriter = new java.io.PrintWriter(writer)
    e.printStackTrace(printWriter)
    writer.toString()
  }
}
