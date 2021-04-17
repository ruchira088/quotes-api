package com.ruchij.dao.lock

import com.ruchij.dao.doobie.DoobieMappings._
import com.ruchij.dao.lock.models.{Lock, LockType}
import doobie.ConnectionIO
import doobie.implicits.toSqlInterpolator
import org.joda.time.DateTime

import java.util.UUID

object DoobieLockDao extends LockDao[ConnectionIO] {
  override type InsertionResult = Int

  val SelectQuery = fr"SELECT id, index, created_at, lock_type, released_at FROM lock"

  override def insert(lock: Lock): ConnectionIO[Int] =
    sql"""
      INSERT INTO lock (id, index, created_at, lock_type, released_at)
        VALUES (${lock.id}, ${lock.index}, ${lock.createdAt}, ${lock.lockType}, ${lock.releasedAt})
    """
      .update
      .run

  override def findLatestByType(lockType: LockType): ConnectionIO[Option[Lock]] =
    (SelectQuery ++ fr"WHERE lock_type = $lockType ORDER BY created_at DESC LIMIT 1")
      .query[Lock]
      .option

  override def findById(id: UUID): ConnectionIO[Option[Lock]] =
    (SelectQuery ++ fr"WHERE id = $id")
      .query[Lock]
      .option

  override def release(id: UUID, timestamp: DateTime): ConnectionIO[Int] =
    sql"UPDATE lock SET released_at = $timestamp WHERE id = $id AND released_at IS NULL"
      .update
      .run
}
