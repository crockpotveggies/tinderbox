package models.bot.tasks.message

/**
 * A list of fun, pre-defined message trees.
 */
object FunMessages {

  def messages = List(
    MessageTree(
      value = "{name} are you a fan of avocados?",
      right = Some(MessageTree(
        value = "So if I asked you to have a guacamole party with me you'd do it?",
        right = None,
        left = None
      )),
      left = Some(MessageTree(
        value = "Do women love anything more than avocados?",
        right = None,
        left = None
      ))
    ),
    MessageTree(
      value = "Can you teach a guy to bake and all that?",
      right = Some(MessageTree(
        value = "How about peach crumble in a crock pot?",
        right = None,
        left = None
      )),
      left = None
    ),
    MessageTree(
      value = "I can't wait to introduce you to my mom!",
      right = Some(MessageTree(
        value = "Since mom showed everyone my naked baby pictures, I've been looking fo ways to get her back.",
        right = None,
        left = None
      )),
      left = Some(MessageTree(
        value = "Please tell me you're at least a fan of moms.",
        right = None,
        left = None
      ))
    )
  )

}
