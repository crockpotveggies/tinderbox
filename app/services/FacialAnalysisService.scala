package services

import akka.actor.{Props, Actor}
import play.api.Logger
import play.api.Play.current
import play.api.libs.concurrent.Akka
import utils.SparkMLLibUtility
import scala.collection.mutable.Map
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits._
import scala.collection.JavaConversions._
import org.mapdb._
import utils.tinder.TinderApi
import utils.tinder.model._
import models.{MatchUpdate, Notification}
import java.util.concurrent.ConcurrentNavigableMap
import java.util.{TimeZone, Date}
import java.text.{SimpleDateFormat, DateFormat}
import org.apache.spark.mllib.clustering.{KMeans, KMeansModel}
import org.apache.spark.mllib.linalg.{Vector, Vectors}

/**
 * A service that analyzes profiles for future recommendations.
 */
object FacialAnalysisService {
  // if we don't set the ClassLoader it will be stuck in SBT
  Thread.currentThread().setContextClassLoader(play.api.Play.classloader)

  /**
   * Vectors are k-means cluster centers created by analyzing RGB values of Tinder faces.
   *
   * @note These vectors are cached here for convenience.
   */
  private val yes_vectors: ConcurrentNavigableMap[String, Array[Vector]] = MapDB.db.getTreeMap("no_vectors")

  private val no_vectors: ConcurrentNavigableMap[String, Array[Vector]] = MapDB.db.getTreeMap("yes_vectors")

  def fetchYesVector(userId: String): Option[Array[Vector]] = yes_vectors.get(userId) match {
    case null => None
    case vectors => Some(vectors)
  }

  def storeYesVector(userId: String, vectors: Array[Vector]) = yes_vectors.put(userId, vectors)

  def fetchNoVector(userId: String): Option[Array[Vector]] = no_vectors.get(userId) match {
    case null => None
    case vectors => Some(vectors)
  }

  def storeNoVector(userId: String, vectors: Array[Vector]) = no_vectors.put(userId, vectors)

  /**
   * k-means model for "yes" faces.
   */
  var yes_kmeans: Option[KMeansModel] = trainKMeans("yes")

  /**
   * k-means model for "no" faces
   */
  var no_kmeans: Option[KMeansModel] = trainKMeans("no")

  /**
   * Helper method for developing a new k-means model.
   */
  def trainKMeans(dataType: String): Option[KMeansModel] = {
    val vectors = dataType match {
      case "yes" => yes_vectors.map { kv => kv._2 }.flatten.toList
      case "no" => no_vectors.map { kv => kv._2 }.flatten.toList
    }

    // parallelize the data into Spark
    val data = SparkMLLibUtility.context.parallelize(vectors)

    // Cluster the data into 20 groups
    val numClusters = 7
    val numIterations = 10
    try {
      val clusters = KMeans.train(data, numClusters, numIterations)
      clusters.clusterCenters.foreach { c => Logger.debug("[recommendations] Cluster center %s of %s for %s kmeans model is %s." format (clusters.clusterCenters.indexOf(c), numClusters, dataType, c.toString))}
      Some(clusters)

    } catch {
      case _ =>
        Logger.warn("[recommendations] No data to train for %s." format dataType)
        None
    }
  }

  /**
   * Actor for performing batch processing of k-means models.
   */
  private class KMeansTask extends Actor {
    def receive = {
      case "tick" =>
        yes_kmeans = trainKMeans("yes")
        no_kmeans = trainKMeans("no")
        Logger.debug("[recommendations] k-means models have been trained.")
    }
  }
  private val kMeansActor = Akka.system.actorOf(Props[KMeansTask], name = "KMeansTask")
  private val kMeansService = {
    Akka.system.scheduler.schedule(5 minutes, 5 minutes, kMeansActor, "tick")
  }

}
