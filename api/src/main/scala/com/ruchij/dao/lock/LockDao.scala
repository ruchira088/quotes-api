package com.ruchij.dao.lock

import com.ruchij.dao.lock.models.{Lock, LockType}
import org.joda.time.DateTime

import java.util.UUID

trait LockDao[F[_]] {
  type InsertionResult

  def insert(lock: Lock): F[InsertionResult]

  def findLatestByType(lockType: LockType): F[Option[Lock]]

  def findById(id: UUID): F[Option[Lock]]

  def release(id: UUID, timestamp: DateTime): F[Int]
}
