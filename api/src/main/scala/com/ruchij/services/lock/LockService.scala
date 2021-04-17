package com.ruchij.services.lock

import com.ruchij.dao.lock.models.{Lock, LockType}

import java.util.UUID

trait LockService[F[_]] {
  def acquireLock(lockType: LockType): F[Option[Lock]]

  def releaseLock(id: UUID): F[Lock]
}
