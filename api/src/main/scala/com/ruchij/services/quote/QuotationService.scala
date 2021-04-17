package com.ruchij.services.quote

import com.ruchij.dao.quote.models.Quote

trait QuotationService[F[_]] {

  def searchByAuthor(author: String, pageSize: Int, offset: Int): F[Seq[Quote]]

  def searchByText(text: String, pageSize: Int, offset: Int): F[Seq[Quote]]

  def retrieveAll(pageSize: Int, offset: Int): F[Seq[Quote]]

}
