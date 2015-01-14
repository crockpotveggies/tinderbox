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
 * Classes for creating message decision trees.
 */
case class MessageTree(
  val value: String,
  val negative: Option[MessageTree] = None,
  val positive: Option[MessageTree] = None

) {

  /**
   * Walk the node using a boolean input.
   * @param step
   * @return
   */
  def walk(step: Boolean): Option[MessageTree] = {
    step match {
      case true => this.positive
      case false => this.negative
    }
  }
}

object MessageTree {
  def observe(node: MessageTree, f: MessageTree => Unit): Unit = {
    f(node)
    node.negative foreach { observe(_, f) }
    node.positive foreach { observe(_, f) }
  }


  /**
   * Walk the tree and retrieve a child using a sequence of boolean steps.
   * @param tree
   * @param steps
   * @return
   */
  def walkTree(tree: MessageTree, steps: Array[Boolean]): Option[MessageTree] = {
    var walks = 0
    var node: Option[MessageTree] = Some(tree)

    // TODO: something cleaner than a try/catch
    try {
      while (walks < steps.size) {
        node = node.get.walk(steps(walks))
        walks += 1
      }
    } catch {
      case e: Throwable =>
        node = None
    }

    node
  }
}