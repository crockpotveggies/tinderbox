package services

import play.api.Play.current
import play.api.Logger
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import akka.actor._
import scala.concurrent.duration._
import org.mapdb._
import java.io.File


/**
 * App database backed by disk.
 */
object MapDB {

  /**
   * Storage files.
   */
  private val dbDirectory = new File("data").mkdir()
  private val dbFile = new File("data/tinderbox_data")

  /**
   * The database context.
   */
  val db = {
    // if we don't set the ClassLoader it will be stuck in SBT
    Thread.currentThread().setContextClassLoader(play.api.Play.classloader)
    // create the DB
    DBMaker.newFileDB(dbFile)
      .closeOnJvmShutdown()
      .encryptionEnable("password")
      .make()
  }

  /**
   * Actor for persisting database to disk periodically.
   */
  private class DBCommitter(database: DB) extends Actor {
    def receive = {
      case "tick" =>
        database.commit()
        Logger.debug("[mapdb] Database committer has persisted data to disk.")
    }
  }
  private val commitActor = Akka.system.actorOf(Props(new DBCommitter(db)), name = "DBCommitter")
  private val commitService = {
    Akka.system.scheduler.schedule(10 seconds, 50 seconds, commitActor, "tick")
  }
}
