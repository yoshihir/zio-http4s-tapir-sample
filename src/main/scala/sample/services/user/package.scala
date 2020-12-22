package sample.services

import sample.data.Error.UserNotFound
import sample.data.{User, UserId}
import sample.persistence.UserPersistence
import zio.clock.Clock
import zio._

package object user {

  type UserService = Has[UserService.Service]

  object UserService {
    trait Service {
      def createUser(user: User): RIO[UserPersistence, User]
      def getUser(userId: UserId): RIO[UserPersistence, User]
      def deleteUser(userId: UserId): RIO[UserPersistence, Unit]
    }

    val live: ULayer[UserService] =
      ZLayer.succeed(
        new UserService.Service {
          override def createUser(user: User): RIO[UserPersistence, User] =
            UserPersistence.create(user)

          override def getUser(userId: UserId): RIO[UserPersistence, User] =
            UserPersistence
              .retrieve(userId)
              .flatMap(maybeUser => Task.require(UserNotFound(userId))(Task.succeed(maybeUser)))

          override def deleteUser(userId: UserId): RIO[UserPersistence, Unit] =
            UserPersistence
              .delete(userId)
              .flatMap {
                case true  => UIO.unit
                case false => Task.fail(UserNotFound(userId))
              }
        }
      )

    def createUser(user: User): RIO[UserService with UserPersistence, User] =
      RIO.accessM(_.get.createUser(user))

    def getUser(userId: UserId): RIO[UserService with UserPersistence, User] =
      RIO.accessM(_.get.getUser(userId))

    def deleteUser(userId: UserId): RIO[UserService with UserPersistence, Unit] =
      RIO.accessM(_.get.deleteUser(userId))
  }

}
