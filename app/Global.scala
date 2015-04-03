import akka.actor.Props
import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import play.api.Play.current
import play.api.libs.concurrent.Akka
import org.fusesource.jansi.Ansi._
import org.fusesource.jansi.Ansi.Color._
import services.TinderBot
import models.bot.BotCommand
/**
 * the big global class that lays the foundation
 */
object Global extends GlobalSettings {

  /**
   * boot jobs to run on application start
   */
  override def onStart(app: Application) {

    val jvmVersion = sys.props("java.specification.version").toDouble
    if (jvmVersion != 1.7)
      throw new RuntimeException(s"Unsupported JRE: java.specification.version $jvmVersion != 1.7")

    // make sure the bot is instantiated and running
    TinderBot.context ! BotCommand("run")

    println(ansi().fg(RED).bg(WHITE).a("""

     |__________________`s__________________
     |__________________s$______________s___
     |_________________.s$$_____________s$__
     |________________s$$$?______s_____s$³__
     |______________.s$$$___ __.s$,___s$$³__
     |_____________s$$$$³______.s$___.$$³___
     |________,____$$$$$.______s$³____³$____
     |________$___$$$$$$s_____s$³_____³,____
     |_______s$___³$$$$$$$s___$$$,____..____
     |_______$$____³$$$$$$s.__³$$s_____,,___
     |________³$.____³$$$$$$$s_.s$$$________
     |_______`$$.____³$$$$$$$_$$$$__ s³_____
     |________³$$s____³$$$$$$s$$$³__ s$³____
     |_________³$$s____$$$$$s$$$$`__ s$$____
     |______s.__$$$$___s$$$$$$$$³_.s $$³____
     |______$$_s$$$$..s$$$$$$$$$$$$$ $³_____
     |______s$.s$$$$s$$$$$$$$$$$$$$$ $______
     |______________________________________
     |
     |    Tinderbox
     |    ===============================
     |    A handy tool for automating and
     |    analyzing the Tinder App

  """).reset() )

  }
}
