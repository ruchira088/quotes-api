package com.ruchij.dao.quote.models

import enumeratum.{Enum, EnumEntry}

sealed trait SortOrder extends EnumEntry {
  val shortName: String
}

object SortOrder extends Enum[SortOrder] {
  case object Ascending extends SortOrder {
    override val shortName: String = "ASC"
  }

  case object Descending extends SortOrder {
    override val shortName: String = "DESC"
  }

  override def values: IndexedSeq[SortOrder] = findValues
}
