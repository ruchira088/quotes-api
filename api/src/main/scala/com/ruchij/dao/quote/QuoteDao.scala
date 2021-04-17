package com.ruchij.dao.quote

import com.ruchij.dao.quote.models.{Quote, SortBy}

import java.util.UUID

trait QuoteDao[F[_]] {
  type InsertionResult

  def insert(quote: Quote): F[InsertionResult]

  def retrieveAll(sortBy: SortBy, size: Int, offset: Int): F[Seq[Quote]]

  def findById(id: UUID): F[Option[Quote]]

  def searchByAuthor(author: String, size: Int, offset: Int): F[Seq[Quote]]

  def searchByText(text: String, size: Int, offset: Int): F[Seq[Quote]]
}
