package com.ruchij.dao.quote.models

case class Paging(pageSize: Int, pageNumber: Int, sortBy: SortBy, sortOrder: SortOrder)

object Paging {
  val Default: Paging = Paging(25, 0, SortBy.CreationDate, SortOrder.Descending)
}
