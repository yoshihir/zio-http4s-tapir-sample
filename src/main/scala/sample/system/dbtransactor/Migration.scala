package sample.system.dbtransactor

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.Location
import org.flywaydb.core.api.configuration.ClassicConfiguration
import sample.system.config.{Config, PostgresConfig}
import zio.{Has, RIO, ZIO}
import zio.logging.{log, Logging}

object Migration {

  private val cpLocation = new Location("classpath:db/migration")
  private val fsLocation = new Location("filesystem:db/migration")

  val migrate: RIO[Has[PostgresConfig] with Logging, Unit] =
    Config.dbConfig
      .flatMap { cfg =>
        ZIO.effect {
          val config = new ClassicConfiguration()
          config.setDataSource(cfg.url.value, cfg.user.value, cfg.password.value)
          config.setLocations(cpLocation, fsLocation)
          val newFlyway = new Flyway(config)
          newFlyway.migrate()
        }.unit
      }
      .tapError(err => log.error(s"Error migrating database: $err."))

}
