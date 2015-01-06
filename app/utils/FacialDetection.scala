package utils

import java.awt.image.BufferedImage
import java.net.URL
import java.awt.Rectangle
import javax.imageio.ImageIO
import jviolajones.Detector
import scala.collection.JavaConversions._

/**
 * Facial detection using the Viola-Jones algorithm.
 *
 * @see https://github.com/tc/jviolajones
 */
class FacialDetection(val fileName: String) extends App {

  def sourceIO = new URL(fileName).openStream()

  /**
   * Retrieve coordinates of detected faces.
   */
  def detectFaces: List[Rectangle] = {
    val detector = new Detector(FacialDetection.haarCascades)
    detector.getFaces(sourceIO, 1.2f, 1.1f, .05f, 2, true).toList
  }

  /**
   * Count the number of faces in the photo.
   */
  def countFaces: Int = detectFaces.size

  /**
   * Extract a list of face images from the source image.
   */
  def extractFaces: List[BufferedImage] = {
    val faces = detectFaces
    val bufferedImage = ImageIO.read(sourceIO)

    faces.map { face => bufferedImage.getSubimage(face.x, face.y, face.width, face.height) }
  }
}

object FacialDetection {

  def apply(fileName: String) = new FacialDetection(fileName)

  val haarCascades = "haarcascade_frontalface_default.xml"

}