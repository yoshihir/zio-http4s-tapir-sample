package sample.http.routes

import cats.syntax.all._

import org.http4s.HttpRoutes
import sample.persistence.UserPersistence
import sample.services.healthcheck.Health.{Healthy, ShuttingDown, Unhealthy}
import sample.services.healthcheck.HealthCheck
import sttp.tapir.json.circe._
import sttp.tapir.server.http4s.ztapir._
import sttp.tapir.ztapir._
import zio.{IO, Task, UIO, URIO, ZIO}

import zio.interop.catz._

object HealthCheckRoutes {

  private val healthCheck: ZIO[HealthCheck with UserPersistence, String, String] =
    HealthCheck.healthStatus.orElseFail("Internal failure.").flatMap {
      case Healthy      => UIO.succeed("Healthy!")
      case Unhealthy    => IO.fail("Unhealthy")
      case ShuttingDown => IO.fail("Shutting Down")
    }

  private val aliveEndpoint: ZEndpoint[Unit, String, String] =
    endpoint.get.in("health" / "alive").errorOut(stringBody).out(jsonBody[String])

  private val readyEndpoint: ZEndpoint[Unit, String, String] =
    endpoint.get.in("health" / "ready").errorOut(stringBody).out(jsonBody[String])

  private val aliveRoute: URIO[HealthCheck with UserPersistence, HttpRoutes[Task]] =
    aliveEndpoint.toRoutesR(_ => healthCheck)
  private val readyRoute: URIO[HealthCheck with UserPersistence, HttpRoutes[Task]] =
    readyEndpoint.toRoutesR(_ => healthCheck)

  val routes: URIO[HealthCheck with UserPersistence, HttpRoutes[Task]] = for {
    aliveRoute <- aliveRoute
    readyRoute <- readyRoute
  } yield aliveRoute <+> readyRoute
}
