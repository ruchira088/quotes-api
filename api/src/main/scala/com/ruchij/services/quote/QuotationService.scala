package com.ruchij.services.quote

import com.ruchij.dao.quote.models.{Paging, Quote}

import java.util.UUID

trait QuotationService[F[_]] {
  def insert(author: String, text: String): F[Quote]

  def findById(id: UUID): F[Quote]

  def find(maybeAuthor: Option[String], maybeText: Option[String], paging: Paging): F[Seq[Quote]]

}