package com.ruchij.services.feeder

import cats.effect.{Clock, Concurrent, Sync}
import cats.implicits._
import cats.{Applicative, ~>}
import com.ruchij.dao.lock.models.LockType
import com.ruchij.dao.quote.QuoteDao
import com.ruchij.dao.quote.models.Quote
import com.ruchij.services.explorer.QuotationExplorer
import com.ruchij.services.feeder.models.FeederResult
import com.ruchij.services.feeder.models.FeederResult.{DataFeedResults, ExistingDataFeed}
import com.ruchij.services.lock.LockService
import com.ruchij.types.Random
import fs2.Stream
import org.joda.time.DateTime

import java.util.UUID
import java.util.concurrent.TimeUnit

class DataFeederImpl[F[+ _]: Concurrent: Random[*[_], UUID]: Clock, G[_]: Applicative](
  lockService: LockService[F],
  explorers: List[QuotationExplorer[F]],
  quoteDao: QuoteDao[G]
)(implicit transaction: G ~> F)
    extends DataFeeder[F] {

  override val run: Stream[F, FeederResult] =
    Stream.eval(lockService.acquireLock(LockType.CrawlQuotes))
      .flatMap {
        case None =>
          Stream.emit(ExistingDataFeed)

        case Some(lock) =>
          explorers
            .foldLeft[Stream[F, Quote]](Stream.empty) {
              case (stream, explorer) => stream.merge(discover(explorer))
            }
            .chunkN(25)
            .evalMap { chunks =>
              transaction(chunks.toList.traverse(quoteDao.insert)).as(chunks.size)
            }
            .onFinalize(lockService.releaseLock(lock.id).productR(Applicative[F].unit))
            .scan(0) { _ + _ }
            .tail
            .evalMap { count => Sync[F].delay(println(s"Saved: $count quotes")).as(DataFeedResults(count)) }
      }

  def discover(quotationExplorer: QuotationExplorer[F]): Stream[F, Quote] =
    quotationExplorer.discover
      .mapAsyncUnordered(100) { discoveredQuote =>
        for {
          id <- Random[F, UUID].generate
          timestamp <- Clock[F].realTime(TimeUnit.MILLISECONDS).map(milliseconds => new DateTime(milliseconds))
        } yield Quote(id, timestamp, discoveredQuote.author, discoveredQuote.text)
      }

}
