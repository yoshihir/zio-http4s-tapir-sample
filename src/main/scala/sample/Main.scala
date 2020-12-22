package sample

import sample.persistence.UserPersistenceSQL
import sample.services.healthcheck.HealthCheck
import sample.system.config.Config
import sample.system.dbtransactor.DBTransactor
import sample.system.logging.Logger
import sample.http.Server
import sample.services.user.UserService
import zio.blocking.Blocking
import zio.logging.log
import zio.{App, ExitCode, ZEnv, ZIO}

object Main extends App {

  val logger     = Logger.live
  val config     = logger >>> Config.live
  val transactor = logger ++ Blocking.any ++ config >>> DBTransactor.live
//  val httpClient      = config >>> Client.live
  val userPersistence = transactor >>> UserPersistenceSQL.live
//  val resReqClient    = config ++ httpClient >>> ReqResClientHTTP.live
  val userManager = UserService.live

  val appLayers = logger ++ config ++ HealthCheck.live ++ userPersistence ++ userManager

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] =
    Server.runServer
      .tapError(err => log.error(s"Execution failed with: $err"))
      .provideCustomLayer(appLayers)
      .exitCode
}
