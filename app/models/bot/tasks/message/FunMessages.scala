package models.bot.tasks.message

/**
 * A list of fun, pre-defined message trees.
 */
object FunMessages {

  def messages = List(
    MessageTree(
      value = "So Tinder says we think each others hot...",
      right = Some(MessageTree(
        value = "Off to a good start",
        right = None,
        left = None
      )),
      left = Some(MessageTree(
        value = "Then you should have swiped left",
        right = None,
        left = None
      ))
    ),
    MessageTree(
      value = "You have a nice face",
      right = Some(MessageTree(
        value = "Not many on here",
        right = None,
        left = None
      )),
      left = Some(MessageTree(
        value = "#ByeFelicia",
        right = None,
        left = None
      ))
    ),
    MessageTree(
      value = "You have 30 mins to get to Rodney's in Yaletown",
      right = Some(MessageTree(
        value = "lol I wanted to see if you took the bait",
        right = None,
        left = None
      )),
      left = Some(MessageTree(
        value = "ok thought I would try!",
        right = None,
        left = None
      ))
    ),
    MessageTree(
      value = "Drink of choice? Vodka or Gin",
      right = Some(MessageTree(
        value = "I'm def and Vodka girl myself! There's a wicked tasting menu at Grain in the Hyatt!",
        right = None,
        left = None
      )),
      left = Some(MessageTree(
        value = "That's too bad",
        right = None,
        left = None
      ))
    )
  )

}
