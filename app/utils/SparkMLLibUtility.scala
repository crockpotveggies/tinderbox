package utils

import play.api.Logger

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf

import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.classification.NaiveBayes

object SparkMLLibUtility {

  val conf = new SparkConf(false) // skip loading external settings
    .setMaster("local[4]") // run locally with enough threads
    .setAppName("firstSparkApp")
    .set("spark.logConf", "true")
    .set("spark.driver.host", "localhost")

  val context = new SparkContext(conf)

  def SparkMLLibExample(fileLocation: String): Double = {

    Logger.info("["+fileLocation+"] Beginning training")

    // default should be "public/data/sample_naive_bayes_data.txt"
    val data = context.textFile(fileLocation)
    val parsedData = data.map { line =>
      val parts = line.split(',')
      LabeledPoint(parts(0).toDouble, Vectors.dense(parts(1).split(' ').map(_.toDouble)))
    }
    // Split data into training (60%) and test (40%).
    val splits = parsedData.randomSplit(Array(0.6, 0.4), seed = 11L)
    val training = splits(0)
    val test = splits(1)

    val model = NaiveBayes.train(training, lambda = 1.0)
    val prediction = model.predict(test.map(_.features))

    val predictionAndLabel = prediction.zip(test.map(_.label))
    val accuracy = 1.0 * predictionAndLabel.filter(x => x._1 == x._2).count() / test.count()

    Logger.info("["+fileLocation+"] Training complete")
    Logger.info("["+fileLocation+"] Accuracy = " + accuracy * 100 + "%")

    accuracy
  }
}