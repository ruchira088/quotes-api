package com.ruchij.dao.lock.models

import org.joda.time.DateTime

import java.util.UUID

case class Lock(id: UUID, index: Int, createdAt: DateTime, lockType: LockType, releasedAt: Option[DateTime])
