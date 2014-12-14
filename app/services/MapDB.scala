package services

import org.mapdb._
import java.io.File


/**
 * App database backed by disk.
 */
object MapDB {

  /**
   * The database context.
   */
  val db = DBMaker.newFileDB(new File("data/tinderbox_data"))
    .closeOnJvmShutdown()
    .encryptionEnable("password")
    .make()
}
