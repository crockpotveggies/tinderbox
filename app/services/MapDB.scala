package services

import play.api.Play.current
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
      .encryptionEnable("password")
      .make()
  }
}
