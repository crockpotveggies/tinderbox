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
   * Compresses two or more repeated messages into a single message.
   * @param messages
   */
  def runLengthConcat(messages: List[Message]): List[Message] = {
    // utility function for packing messages
    def pack[A](ls: List[(String, Message)]): List[List[(String, Message)]] = {
      if (ls.isEmpty) List(List())
      else {
        val (packed, next) = ls span { _._1 == ls.head._1 }
        if (next == Nil) List(packed)
        else packed :: pack(next)
      }
    }
    // utility function for concatenating the messages
    def concat(ls: List[List[(String, Message)]]): List[Message] = {
      ls.map{ sublist =>
        val concatmessage = sublist.map(_._2).map(_.message).mkString(" ")
        sublist.last._2.copy(message = concatmessage)
      }
    }

    // create ID tuples since users send multiple messages
    val curried = messages.map( o => (o.from, o))

    // now pack and compress the messages
    concat(pack(curried))
  }

  /**
   * Assigns a boolean representing sentiment to each message and returns them in tuples.
   * @param messages
   */
  def assignSentimentDirection(messages: List[Message]): List[(Message, Direction)] = {
    // it's necessary we concatenate repeated messages, otherwise the tree could be terminated early
    runLengthConcat(messages)
      .map { m =>
        val sentiment = MessageSentiment.findSentiment(m.message) match {
          case MessageSentiment.NEGATIVE => Left
          case MessageSentiment.POSITIVE => Right
          case MessageSentiment.NEUTRAL  => Right
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
