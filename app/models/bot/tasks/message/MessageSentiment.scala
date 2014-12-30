package models.bot.tasks.message

import java.util.Properties
import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer
import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.pipeline.Annotation
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations
import edu.stanford.nlp.trees.Tree
import edu.stanford.nlp.util.CoreMap

import scala.collection.mutable


/**
 * Utility for analyzing sentiment of a message.
 *
 * @see https://github.com/shekhargulati/day20-stanford-sentiment-analysis-demo
 */
object MessageSentiment {

  val nlpProps = {
    val props = new Properties()
    props.setProperty("annotators", "tokenize, ssplit, parse, sentiment")
    props
  }

  def findSentiment(message: String): SENTIMENT_TYPE = {
    val pipeline = new StanfordCoreNLP(nlpProps)

    val annotation = pipeline.process(message)
    var sentiments: ListBuffer[Double] = ListBuffer()

    for (sentence <- annotation.get(classOf[CoreAnnotations.SentencesAnnotation])) {
      val tree = sentence.get(classOf[SentimentCoreAnnotations.AnnotatedTree])
      val sentiment = RNNCoreAnnotations.getPredictedClass(tree)
      val partText = sentence.toString

      sentiments += sentiment.toDouble
    }

    val averageSentiment:Double = {
      if(sentiments.size > 0) sentiments.sum / sentiments.size
      else 2
    }
    /* note here that we're leaving a gap between 1.6 and 2 for neutral classification
       because the default model trained for coreNLP can classify sarcasm and humor
       as negative when really in Tinder's context it can be positive */
    averageSentiment match {
      case s if s < 0.0 => NOT_UNDERSTOOD
      case s if s < 1.6 => NEGATIVE
      case s if s <= 2.0 => NEUTRAL
      case s if s < 5.0 => POSITIVE
      case s if s >= 5.0 => NOT_UNDERSTOOD
    }
  }

  trait SENTIMENT_TYPE
  case object NEGATIVE extends SENTIMENT_TYPE
  case object NEUTRAL extends SENTIMENT_TYPE
  case object POSITIVE extends SENTIMENT_TYPE
  case object NOT_UNDERSTOOD extends SENTIMENT_TYPE

}
