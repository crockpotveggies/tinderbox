package models.bot.tasks.message

/**
 * A list of fun, pre-defined message trees.
 */
object FunMessages {

  def messages = List(
    MessageTree(
      value = "{name} are you a fan of avocados?",
      positive = Some(MessageTree(
        value = "So if I asked you to have a guacamole party with me you'd do it?",
        positive = None,
        negative = None
      )),
      negative = Some(MessageTree(
        value = "Do women love anything more than avocados?",
        positive = None,
        negative = None
      ))
    ),
    MessageTree(
      value = "Can you teach a guy to bake and all that?",
      positive = Some(MessageTree(
        value = "How about peach crumble in a crock pot?",
        positive = None,
        negative = None
      )),
      negative = None
    ),
    MessageTree(
      value = "I can't wait to introduce you to my mom!",
      positive = Some(MessageTree(
        value = "Since mom showed everyone my naked baby pictures, I've been looking fo ways to get her back.",
        positive = None,
        negative = None
      )),
      negative = Some(MessageTree(
        value = "Please tell me you're at least a fan of moms.",
        positive = None,
        negative = None
      ))
    )
  )

}
