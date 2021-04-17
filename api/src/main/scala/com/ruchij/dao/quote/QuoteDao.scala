package com.ruchij.dao.quote

import com.ruchij.dao.quote.models.{Paging, Quote}

import java.util.UUID

trait QuoteDao[F[_]] {
  type InsertionResult

  def insert(quote: Quote): F[InsertionResult]

  def findById(id: UUID): F[Option[Quote]]

  def find(maybeAuthor: Option[String], maybeText: Option[String], paging: Paging): F[Seq[Quote]]
}
