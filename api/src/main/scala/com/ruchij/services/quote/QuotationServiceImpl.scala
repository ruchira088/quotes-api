package com.ruchij.services.quote

import cats.effect.Clock
import cats.implicits._
import cats.{Applicative, ApplicativeError, MonadError, ~>}
import com.ruchij.dao.quote.QuoteDao
import com.ruchij.dao.quote.models.{Paging, Quote}
import com.ruchij.exceptions.ResourceNotFoundException
import com.ruchij.types.Random
import org.joda.time.DateTime

import java.util.UUID
import java.util.concurrent.TimeUnit

class QuotationServiceImpl[F[+_] : Random[*[_], UUID]: Clock: MonadError[*[_], Throwable], G[_]](quoteDao: QuoteDao[G])(implicit transaction: G ~> F)
  extends QuotationService[F] {

  override def insert(author: String, text: String): F[Quote] =
    for {
      id <- Random[F, UUID].generate
      timestamp <- Clock[F].realTime(TimeUnit.MILLISECONDS).map(timestamp => new DateTime(timestamp))

      quote = Quote(id, timestamp, author, text)

      _ <- transaction(quoteDao.insert(quote))
    }
    yield quote

  override def findById(id: UUID): F[Quote] =
    transaction(quoteDao.findById(id))
      .flatMap {
        _.fold[F[Quote]](ApplicativeError[F, Throwable].raiseError(ResourceNotFoundException(s"Quote not found with id = $id"))) {
          quote => Applicative[F].pure(quote)
        }
      }

  override def find(maybeAuthor: Option[String], maybeText: Option[String], paging: Paging): F[Seq[Quote]] =
    transaction(quoteDao.find(maybeAuthor, maybeText, paging))
}
