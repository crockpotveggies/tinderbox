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
  val left: Option[MessageTree] = None,
  val right: Option[MessageTree] = None

) {

  /**
   * Walk the node using a boolean input.
   * @param direction
   * @return
   */
  def walk(direction: Direction): Option[MessageTree] = {
    direction match {
      case Right => this.left
      case Left => this.right
    }
  }
}

object MessageTree {
  def observe(node: MessageTree, f: MessageTree => Unit): Unit = {
    f(node)
    node.left foreach { observe(_, f) }
    node.right foreach { observe(_, f) }
  }


  /**
   * Walk the tree and retrieve a child using a sequence of boolean steps.
   * @param tree
   * @param steps
   */
  def walkTree(tree: MessageTree, steps: List[Direction]): Option[MessageTree] = {
    val treeWalk: Option[MessageTree] = Some(tree) // workaround for type bug
    (steps foldLeft treeWalk)((t, direction) => t flatMap (_ walk direction))
  }
}

sealed abstract class Direction
object Left extends Direction
object Right extends Direction