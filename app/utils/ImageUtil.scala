package utils

import java.awt.Color
import java.awt.image.{DataBufferByte, BufferedImage}
import org.imgscalr.Scalr
import play.api.Logger


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
   * Converts an image to grayscale and retrieves its image pixels.
   * @param image
   * @return
   */
  def getNormalizedImagePixels(image: BufferedImage, width: Int, height: Int): Array[Double] = {
    val scaledImage = Scalr.resize(image, Scalr.Method.BALANCED, Scalr.Mode.FIT_TO_HEIGHT, width, height)
    val greyImage: BufferedImage = new ImageNormalizer().getNormalizedValues(scaledImage)

    // convert to grayscale image
    val bytePixels: Array[Byte] = (greyImage.getRaster.getDataBuffer.asInstanceOf[DataBufferByte]).getData

    val doublePixels: Array[Double] = new Array[Double](bytePixels.length)

    (0 to (doublePixels.length-1)).foreach { i =>
      doublePixels(i) = (bytePixels(i) & 255).asInstanceOf[Double]
    }

    doublePixels
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

  /**
   * Writes an image to a file from a pixel array.
   * @param filename
   * @param imagePixels
   * @param width
   * @param height
   * @param overwrite
   */
  def writeImage(filename: String, imagePixels: Array[Double], width: Int, height: Int, overwrite: Boolean=true): Unit = {
    val meanImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
    val raster = meanImage.getRaster();

    // convert byte array to byte array
    val pixels = new Array[Int](imagePixels.length)
    (0 to (imagePixels.length-1)).foreach { i =>
      pixels(i) = imagePixels(i).toInt
    }
    raster.setPixels(0, 0, width, height, pixels)

    val file = new java.io.File(filename)
    if(overwrite && file.exists()) {
      file.delete()
    }
    javax.imageio.ImageIO.write(meanImage, "gif", file);
  }

  /**
   * Writes an image to a file from a buffered image.
   * @param filename
   * @param image
   * @param overwrite
   */
  def writeBufferedImage(filename: String, image: BufferedImage, overwrite: Boolean=true): Unit = {
    val file = new java.io.File(filename)
    if(overwrite && file.exists()) {
      file.delete()
    }
    javax.imageio.ImageIO.write(image, "gif", file);
  }

}
