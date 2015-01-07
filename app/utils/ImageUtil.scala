package utils

import java.awt.Color
import java.awt.image.BufferedImage
import play.api.Logger
import scala.collection.JavaConversions._


/**
 * Generic utility for image manipulation.
 */
object ImageUtil {

  /**
   * Converts a colored BufferedImage to grayscale.
   * @param image
   * @return
   */
  def toGrayscale(image: BufferedImage): BufferedImage = {
    val width = image.getWidth
    val height = image.getHeight
    val grayImg = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY)
    val g = grayImg.createGraphics()
    g.drawImage(image, 0, 0, null)

    (0 to (width-1)).foreach { x =>
      (0 to (height-1)).foreach { y =>
        val color = grayImg.getRGB(x, y)
        grayImg.setRGB(x, y, color)
      }
    }

    grayImg
  }

  /**
   * Normalizes a list of RGB values so that pixels are evenly distributed.
   * @param images
   * @return
   */
  def getNormalizedGrayValues(images: List[BufferedImage]): List[Array[Int]] = {
    getRGBValues( images.map( img => new ImageNormalizer().getNormalizedValues(img) ) )
  }

  /**
   * Returns aggregate RGB pixel values for a list of images.
   *
   * @note Images should all be of the same person or subject if using for facial analysis.
   * @param images
   */
  def getRGBValues(images: List[BufferedImage]): List[Array[Int]] = {
    images.map { img =>
      val w = img.getWidth
      val h = img.getHeight

      (0 to (w-1)).map { x =>
        (0 to (h-1)).map { y =>
          try {
            val color = new Color(img.getRGB(x, y))
            List(Array[Int](color.getRed, color.getGreen, color.getBlue))

          } catch {
            case e: Exception =>
              Logger.warn("[imageutil] There was an issue extracting RGB values from an image.")
              List()
          }
        }.flatten
      }.flatten
    }.flatten
  }

}
