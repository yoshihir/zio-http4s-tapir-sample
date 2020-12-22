package sample.services

import sample.persistence.UserPersistence
import sample.services.healthcheck.Health.{Healthy, Unhealthy}
import zio.{Has, RIO, ULayer, ZLayer}

package object healthcheck {

  type HealthCheck = Has[HealthCheck.Service]

  object HealthCheck {

    trait Service {
      def healthStatus: RIO[UserPersistence, Health]
    }

    val live: ULayer[HealthCheck] = ZLayer.succeed(
      new Service {
        override def healthStatus: RIO[UserPersistence, Health] =
          UserPersistence.isHealthy.map {
            case true  => Healthy
            case false => Unhealthy
          }
      }
    )
    val healthStatus: RIO[HealthCheck with UserPersistence, Health] = RIO.accessM(_.get.healthStatus)

  }

}
