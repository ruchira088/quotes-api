package com.ruchij.dao.quote.models

import enumeratum.{Enum, EnumEntry}

sealed trait SortBy extends EnumEntry

object SortBy extends Enum[SortBy] {
  case object CreationDate extends SortBy
  case object Author extends SortBy
  case object Text extends SortBy

  override def values: IndexedSeq[SortBy] = findValues
}
