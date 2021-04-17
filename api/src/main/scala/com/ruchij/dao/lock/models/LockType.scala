package com.ruchij.dao.lock.models

import enumeratum.{Enum, EnumEntry}

sealed trait LockType extends EnumEntry

object LockType extends Enum[LockType] {
  case object CrawlQuotes extends LockType

  override def values: IndexedSeq[LockType] = findValues
}
