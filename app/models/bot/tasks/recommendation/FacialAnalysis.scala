package models.bot.tasks.recommendation

import scala.collection.JavaConversions._
import java.awt.Color
import java.awt.image.BufferedImage
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import utils.SparkMLLibUtility
import play.api.Logger


/**
 * Utility for analyzing the RGB values of detected faces.
 */
object FacialAnalysis {

  /**
   * Returns aggregate RGB pixel values for a list of faces.
   *
   * @note Faces should all be of the same person or subject.
   * @param faces
   */
  def getRGBValues(faces: List[BufferedImage]): List[Array[Int]] = {
    faces.map { face =>
      val w = face.getWidth
      val h = face.getHeight

      (0 to (w-1)).map { x =>
        (0 to (h-1)).map { y =>
          try {
            val color = new Color(face.getRGB(x, y))
            List(Array[Int](color.getRed, color.getGreen, color.getBlue))

          } catch {
            case e: Exception =>
              Logger.warn("[facial_analysis] There was an issue extracting RGB values from an image.")
              List()
          }
        }.flatten
      }.flatten
    }.flatten
  }

  /**
   * Collect the k-means cluster centers.
   *
   * @note this is used for future comparison during recommendations.
   */
  def kMeans(rgbs: List[Array[Int]]): Array[Vector] = {
    // parallelize the data into Spark
    val data = SparkMLLibUtility.context.parallelize(rgbs)
    val parsedData = data.map(s => Vectors.dense(s.map(_.toDouble)))

    // Cluster the data into 20 groups
    val numClusters = 7
    val numIterations = 10
    val clusters = KMeans.train(parsedData, numClusters, numIterations)
    clusters.clusterCenters
  }

}
