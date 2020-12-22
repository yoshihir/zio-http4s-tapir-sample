package sample.persistence

import doobie.Transactor
import doobie.implicits._
import doobie.refined.implicits._
import doobie.{Query0, Update0}
import sample.data.Error.DatabaseError
import sample.data.{User, UserId}
import sample.persistence.UserPersistenceSQL.Queries
import sample.system.dbtransactor.DBTransactor
import zio.interop.catz._
import zio.{Task, ZLayer}

final class UserPersistenceSQL(trx: Transactor[Task]) extends UserPersistence.Service {

  def isHealthy: Task[Boolean] =
    Queries.health.option
      .transact(trx)
      .isSuccess

  override def retrieve(userId: UserId): Task[Option[User]] =
    Queries
      .get(userId)
      .option
      .transact(trx)
      .mapError(err => DatabaseError(err.getMessage))

  override def create(user: User): Task[User] =
    Queries
      .create(user)
      .run
      .transact(trx)
      .bimap(err => DatabaseError(err.getMessage), _ => user)

  override def delete(userId: UserId): Task[Boolean] =
    Queries
      .delete(userId)
      .run
      .transact(trx)
      .bimap(
        err => DatabaseError(err.getMessage),
        rows => if (rows == 0) false else true
      )
}

object UserPersistenceSQL {

  object Queries {

    val health: Query0[Unit] = sql"""SELECT 1 as one;""".query[Unit]

    def get(userId: UserId): Query0[User] =
      sql"""SELECT * FROM USERS WHERE ID = ${userId.value} """.query[User]

    def create(user: User): Update0 =
      sql"""INSERT INTO USERS (id, email, first_name, last_name) VALUES (${user.id.value}, ${user.email.value}, ${user.first_name.value}, ${user.last_name.value})""".update

    def delete(userId: UserId): Update0 =
      sql"""DELETE FROM USERS WHERE id = ${userId.value}""".update

  }

  val live: ZLayer[DBTransactor, Throwable, UserPersistence] =
    ZLayer.fromEffect(
      DBTransactor.transactor.map(new UserPersistenceSQL(_))
    )

}
