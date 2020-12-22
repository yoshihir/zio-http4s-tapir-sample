import sample.persistence.UserPersistence
import sample.services.healthcheck.HealthCheck
import sample.services.user.UserService
import sample.system.config.Config
import zio._
import zio.clock.Clock
import zio.logging.Logging

package object sample {
  type UserServiceEnv = UserService with UserPersistence with Logging with Clock
  type AppEnv         = UserServiceEnv with ZEnv with Config with HealthCheck

}
