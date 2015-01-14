package models.bot.tasks.message

import utils.tinder.model.Message
import utils.TimeUtil


/**
 * Utility for organizing Tinder messages.
 */
object MessageUtil {

  /**
   * Filters a list of messages and returns only the other user's messages.
   * @param userId
   * @param messages
   */
  def filterSenderMessages(userId: String, messages: List[Message]): List[Message] = {
    messages.filterNot( m => m.from==userId )
  }

  /**
   * Assigns a boolean representing sentiment to each message and returns them in tuples.
   * @param messages
   */
  def assignSentiment(messages: List[Message]): List[(Message, Boolean)] = {
    messages.map { m =>
      val sentiment = MessageSentiment.findSentiment(m.message) match {
        case MessageSentiment.NEGATIVE => false
        case MessageSentiment.POSITIVE => true
        case MessageSentiment.NEUTRAL  => true
      }
      (m, sentiment)
    }
  }

  /**
   * Sorts through a list of messages and checks whether a reply has been made.
   * @param messages
   */
  def checkIfReplied(userId: String, messages: List[Message]): Boolean = {
    val sorted = messages
      .map { m => (TimeUtil.parseISO8601(m.created_date).getTime, m) }
      .sortBy(_._1)

    sorted.lastOption match {
      case None => false
      case Some(m) if m._2.from!=userId => false
      case Some(m) if m._2.from==userId => true
    }
  }

  /**
   * Extracts the introductory message, useful for lookups of message trees.
   * @param userId
   * @param messages
   */
  def extractIntroMessage(userId: String, messages: List[Message]): Option[String] = {
    val sorted = messages
      .map { m => (TimeUtil.parseISO8601(m.created_date).getTime, m) }
      .sortBy(_._1)

    sorted.headOption match {
      case None => None
      case Some(m) => Some(m._2.message)
    }
  }

}
