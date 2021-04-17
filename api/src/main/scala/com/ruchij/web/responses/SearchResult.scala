package com.ruchij.web.responses

import com.ruchij.dao.quote.models.{Quote, SortBy, SortOrder}

case class SearchResult(
  author: Option[String],
  text: Option[String],
  pageSize: Int,
  pageNumber: Int,
  sortBy: SortBy,
  sortOrder: SortOrder,
  results: Seq[Quote]
)
