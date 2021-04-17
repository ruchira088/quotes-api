package com.ruchij.dao.quote.models

import enumeratum.{Enum, EnumEntry}

sealed trait SortOrder extends EnumEntry

object SortOrder extends Enum[SortOrder] {
  case object Ascending extends SortOrder
  case object Descending extends SortOrder

  override def values: IndexedSeq[SortOrder] = findValues
}
