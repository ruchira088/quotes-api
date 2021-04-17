package com.ruchij.services.lock

import cats.effect.Clock
import cats.implicits._
import cats.{Applicative, ApplicativeError, Monad, MonadError, ~>}
import com.ruchij.dao.lock.LockDao
import com.ruchij.dao.lock.models.{Lock, LockType}
import com.ruchij.exceptions.ResourceNotFoundException
import com.ruchij.syntax._
import com.ruchij.types.Random
import org.joda.time.DateTime

import java.util.UUID
import java.util.concurrent.TimeUnit

class LockServiceImpl[F[+ _]: Clock: Monad: Random[*[_], UUID], G[_]: MonadError[*[_], Throwable]](lockDao: LockDao[G])(
  implicit transaction: G ~> F
) extends LockService[F] {

  override def acquireLock(lockType: LockType): F[Option[Lock]] =
    for {
      id <- Random[F, UUID].generate
      timestamp <- Clock[F].realTime(TimeUnit.MILLISECONDS).map(milliseconds => new DateTime(milliseconds))

      maybeLock <- transaction {
        lockDao.findLatestByType(lockType).flatMap[Option[Lock]] { maybeLock =>
          if (maybeLock.exists(_.releasedAt.isEmpty)) Applicative[G].pure(None)
          else {
            val lock = Lock(id, maybeLock.map(_.index + 1).getOrElse(0), timestamp, lockType, None)

            lockDao.insert(lock).as(Some(lock))
          }
        }
      }
    } yield maybeLock

  override def releaseLock(id: UUID): F[Lock] =
    Clock[F]
      .realTime(TimeUnit.MILLISECONDS)
      .flatMap { milliseconds =>
        transaction {
          lockDao
            .release(id, new DateTime(milliseconds))
            .flatMap[Lock] {
              case 0 =>
                ApplicativeError[G, Throwable].raiseError(new IllegalArgumentException("Unable to release lock"))

              case 1 =>
                lockDao.findById(id).to[Throwable](ResourceNotFoundException(s"Unable to find lock with ID = $id"))

              case _ =>
                ApplicativeError[G, Throwable].raiseError(
                  new IllegalStateException("Multiple locks cannot have the same ID")
                )
            }
        }
      }

}
