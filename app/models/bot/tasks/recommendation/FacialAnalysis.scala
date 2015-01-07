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

  def KMEANS_CLUSTERS = 8

  /**
   * Collect the k-means cluster centers.
   *
   * @note this is used for future comparison during recommendations.
   */
  def kMeans(rgbs: List[Array[Int]]): Array[Vector] = {
    // parallelize the data into Spark
    val data = SparkMLLibUtility.context.parallelize(rgbs)
    val parsedData = data.map(s => Vectors.dense(s.map(_.toDouble)))

    //
    val numClusters = KMEANS_CLUSTERS
    val numIterations = 5
    val clusters = KMeans.train(parsedData, numClusters, numIterations)
    clusters.clusterCenters
  }

}
