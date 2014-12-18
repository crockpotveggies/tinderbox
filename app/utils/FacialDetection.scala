package utils

import java.net.URL
import jviolajones.Detector
import scala.collection.JavaConversions._

/**
 * Facial detection using the Viola-Jones algorithm.
 *
 * @see https://github.com/tc/jviolajones
 */
class FacialDetection(val fileName: String) extends App {

  /**
   * Count the number of faces in the photo.
   */
  def countFaces: Int = {
    val detector = new Detector(FacialDetection.haarCascades)
    val list = detector.getFaces(new URL(fileName).openStream() , 1.2f, 1.1f, .05f, 2, true)
    list.size
  }
}

object FacialDetection {

  def apply(fileName: String) = new FacialDetection(fileName)

  val haarCascades = "haarcascade_frontalface_default.xml"

}