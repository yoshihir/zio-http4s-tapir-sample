package sample

import zio.{Has, RIO, Task}
import sample.data.{User, UserId}

package object persistence {

  type UserPersistence = Has[UserPersistence.Service]

  object UserPersistence {

    trait Service {
      def isHealthy: Task[Boolean]
      def retrieve(userId: UserId): Task[Option[User]]
      def create(user: User): Task[User]
      def delete(userId: UserId): Task[Boolean]
    }

    def isHealthy: RIO[UserPersistence, Boolean]                     = RIO.accessM(_.get.isHealthy)
    def retrieve(userId: UserId): RIO[UserPersistence, Option[User]] = RIO.accessM(_.get.retrieve(userId))
    def create(user: User): RIO[UserPersistence, User]               = RIO.accessM(_.get.create(user))
    def delete(userId: UserId): RIO[UserPersistence, Boolean]        = RIO.accessM(_.get.delete(userId))
  }

}
