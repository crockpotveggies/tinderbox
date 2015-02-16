package services

import java.awt.image.BufferedImage

import akka.actor.{Props, Actor}
import cern.colt.matrix.DoubleMatrix2D
import cern.colt.matrix.impl.DenseDoubleMatrix2D
import play.api.Logger
import play.api.Play.current
import play.api.libs.concurrent.Akka
import utils.ErrorWriter
import utils.face.{EigenFaces, MatrixHelpers}
import utils.ImageUtil
import scala.collection.mutable.Map
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits._
import scala.collection.JavaConversions._
import java.util.concurrent.ConcurrentNavigableMap

/**
 * A service that analyzes profiles for future recommendations.
 */
object FacialAnalysisService {
  // if we don't set the ClassLoader it will be stuck in SBT
  Thread.currentThread().setContextClassLoader(play.api.Play.classloader)

  // constants
  val DEFAULT_FACE_SIZE = 200
  val MINIMUM_MODEL_SIZE = 60

  /**
   * User-picked yes/no's are stored here.
   *
   * @note structure in Map is (session.user._id, (user_id, yes/no) )
   */
  private val yesno_data: ConcurrentNavigableMap[String, Map[String, Boolean]] = MapDB.db.getTreeMap("yesno_data")

  /**
   * Vectors are k-means cluster centers created by analyzing RGB values of Tinder faces.
   *
   * @note These vectors are cached here for convenience.
   */
  private val yes_pixels: ConcurrentNavigableMap[String, Map[String, List[Array[Double]]]] = MapDB.db.getTreeMap("no_pixels_source")

  private val no_pixels: ConcurrentNavigableMap[String, Map[String, List[Array[Double]]]] = MapDB.db.getTreeMap("yes_pixels_source")


  /*
   * Functions for managing likes/dislikes, later used to compile facial analysis tasks.
   */
  def storeYesNoData(userId: String, matchUser: String, isLike: Boolean) {
    resetModels(userId, matchUser)
    yesno_data.get(userId) match {
      case null =>
        yesno_data.put(userId, Map(matchUser -> isLike))
      case data =>
        data.put(matchUser, isLike)
        yesno_data.put(userId, data)
    }
  }

  def fetchYesNoData(userId: String): Option[Map[String, Boolean]] = {
    yesno_data.get(userId) match {
      case null =>
        None
      case data =>
        Some(data)
    }
  }

  def resetYesNoData(userId: String, matchUser: String) {
    yesno_data.get(userId) match {
      case null =>
        None
      case data =>
        data.remove(matchUser)
        yesno_data.put(userId, data)
    }
  }

  /*
   * Functions for retrieving processed face pixels for likes/dislikes.
   */
  def fetchYesPixels(userId: String, matchUser: String): Option[List[Array[Double]]] = yes_pixels.get(userId) match {
    case null => None
    case pixels =>
      pixels.get(matchUser)
  }

  def fetchYesPixels(userId: String): Option[Map[String, List[Array[Double]]]] = yes_pixels.get(userId) match {
    case null => None
    case data =>
      Some(data)
  }

  def appendYesPixels(userId: String, matchUser: String, pixels: List[Array[Double]]) = fetchYesPixels(userId) match {
    case None => yes_pixels.put(userId, Map(matchUser -> pixels))
    case Some(data) =>
      data.get(matchUser) match {
        case None =>
          data.put(matchUser, pixels)
          yes_pixels.put(userId, data)
        case Some(userData) =>
          val newData = userData ::: pixels
          data.put(matchUser, newData)
          yes_pixels.put(userId, data)
      }

  }

  def fetchNoPixels(userId: String, matchUser: String): Option[List[Array[Double]]] = no_pixels.get(userId) match {
    case null => None
    case pixels =>
      pixels.get(matchUser)
  }

  def fetchNoPixels(userId: String): Option[Map[String, List[Array[Double]]]] = no_pixels.get(userId) match {
    case null => None
    case data =>
      Some(data)
  }

  def appendNoPixels(userId: String, matchUser: String, pixels: List[Array[Double]]) = fetchNoPixels(userId) match {
    case None => no_pixels.put(userId, Map(matchUser ->  pixels))
    case Some(data) =>
      data.get(matchUser) match {
        case None =>
          data.put(matchUser, pixels)
          no_pixels.put(userId, data)
        case Some(userData) =>
          val newData = userData ::: pixels
          data.put(matchUser, newData)
          no_pixels.put(userId, data)
      }
  }

  /*
   * Check that there are enough models to perform proper calculations
   */
  def modelsAreValid(userId: String): Boolean = {
    val yesModelSize = fetchYesPixels(userId) collect {
      case l: Map[String, List[Array[Double]]] => l.filterNot{ kv => kv._2.size==0 }.map(_._2).flatten.size
    } getOrElse 0
    val noModelSize = fetchNoPixels(userId) collect {
      case l: Map[String, List[Array[Double]]] => l.filterNot{ kv => kv._2.size==0 }.map(_._2).flatten.size
    } getOrElse 0

    yesModelSize!=0 || noModelSize!=0
  }

  /*
   * Functions for resetting models.
   */
  def resetModels(userId: String): Unit = {
    // first erase pixel data
    yes_pixels.remove(userId)
    no_pixels.remove(userId)
    // then erase yesno data
    yesno_data.remove(userId)
    // finally erase models
    yes_models.remove(userId)
    no_models.remove(userId)
  }

  def resetModels(userId: String, matchUser: String): Unit = {
    resetYesNoData(userId, matchUser)
    fetchNoPixels(userId) collect { case m: Map[_, _] => m.remove(matchUser); no_pixels.put(userId, m) }
    fetchYesPixels(userId) collect { case m: Map[_, _] => m.remove(matchUser); yes_pixels.put(userId, m) }
  }

  /*
   * Models used to calculate EigenFace distance.
   */
  val yes_models: ConcurrentNavigableMap[String, (Array[Double], DoubleMatrix2D)] = MapDB.db.getTreeMap("yes_eigen_models")

  val no_models: ConcurrentNavigableMap[String, (Array[Double], DoubleMatrix2D)] = MapDB.db.getTreeMap("no_eigen_models")

  /**
   * Helper method for developing a new eigen faces model.
   */
  def computeAverageFace(userId: String, dataType: String): (Array[Double], DoubleMatrix2D) = {
    // note that placeholder pixels are filtered to not corrupt the eigenfaces
    val raw = dataType match {
      case "yes" => fetchYesPixels(userId).get.filterNot{ kv => kv._2.size==0 }.toList
      case "no" => fetchNoPixels(userId).get.filterNot{ kv => kv._2.size==0 }.toList
    }

    // return models that are valid
    val vectors = raw.map { kv => kv._2.filter(_.size==(DEFAULT_FACE_SIZE * DEFAULT_FACE_SIZE)) }.flatten

    Logger.debug("[recommendations] Found %s pixel sets for %s models." format (vectors.size, dataType))
    if(vectors.size == 0) { throw new java.io.IOException("Pixel lists (type %s) are empty." format dataType) }

    // merge all of the models into a single pixel matrix and compute the average
    val pixelMatrix = MatrixHelpers.mergePixelMatrices(vectors, DEFAULT_FACE_SIZE, DEFAULT_FACE_SIZE)
    val averagePixels = EigenFaces.computeAverageFace(pixelMatrix)

    // normalize the image for presentation
    val averagePixelsNormalized = {
      ImageUtil.getImagePixels(
        new utils.ImageNormalizer().getNormalizedValues(
          ImageUtil.reconstructImage(averagePixels, DEFAULT_FACE_SIZE, DEFAULT_FACE_SIZE)
        ), DEFAULT_FACE_SIZE, DEFAULT_FACE_SIZE
      )
    }
    // write the image to disk for visualization
    ImageUtil.writeImage("data/%s_mean_%s_model.gif".format(userId, dataType), averagePixelsNormalized, DEFAULT_FACE_SIZE, DEFAULT_FACE_SIZE)

    // return the average pixels and eigen faces
    (averagePixels, EigenFaces.computeEigenFaces(pixelMatrix, averagePixels))
  }

  /**
   * Actor for performing batch processing of k-means models.
   */
  private class FaceAnalysisTask extends Actor {
    def receive = {
      case "tick" =>
        TinderService.activeUsers.foreach { userId =>
          try {
            // generate and save the models
            val yesModels = computeAverageFace(userId, "yes")
            yes_models.put(userId, yesModels)
            val noModels = computeAverageFace(userId, "no")
            no_models.put(userId, noModels)

            Logger.debug("[recommendations] Face models have been developed for user %s." format userId)

          } catch {
            case e: Throwable =>
              Logger.warn("[recommendations] Could not yet build face models for user %s because: \n%s" format (userId, ErrorWriter.writeString(e)))
          }
        }
    }
  }
  private val faceAnalysisActor = Akka.system.actorOf(Props[FaceAnalysisTask], name = "FaceAnalysisService")
  private val faceAnalysisService = {
    Akka.system.scheduler.schedule(20 seconds, 5 minutes, faceAnalysisActor, "tick")
  }

}
