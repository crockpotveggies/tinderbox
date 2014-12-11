package utils

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf

object SimpleUtility {

  def simpleApp {
    val logFile = "public/data/README.md" // Should be some file on your system
    val conf = new SparkConf(false) // skip loading external settings
      .setMaster("local[4]") // run locally with enough threads
      .setAppName("firstSparkApp")
      .set("spark.logConf", "true")
      .set("spark.driver.host", "localhost")
    val sc = new SparkContext(conf)
    val logData = sc.textFile(logFile, 4).cache()
    val numSparks = logData.filter(line => line.contains("Spark")).count()
  }

}