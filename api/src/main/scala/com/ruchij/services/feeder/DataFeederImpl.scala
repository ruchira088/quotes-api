package com.ruchij.services.feeder

import cats.effect.{Clock, Concurrent, Timer}
import cats.implicits._
import cats.{Applicative, ~>}
import com.ruchij.dao.lock.models.LockType
import com.ruchij.dao.quote.QuoteDao
import com.ruchij.dao.quote.models.Quote
import com.ruchij.services.explorer.QuotationExplorer
import com.ruchij.services.feeder.models.FeederResult
import com.ruchij.services.feeder.models.FeederResult.{DataFeedResults, ExistingDataFeed}
import com.ruchij.services.hash.HashingService
import com.ruchij.services.lock.LockService
import com.ruchij.syntax.toLoggerF
import com.ruchij.types.{LoggerF, Random}
import com.typesafe.scalalogging.Logger
import fs2.Stream
import org.joda.time.DateTime

import java.util.UUID
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.DurationInt

class DataFeederImpl[F[+ _]: Concurrent: Random[*[_], UUID]: Timer, G[_]: Applicative](
  lockService: LockService[F],
  hashingService: HashingService[F],
  explorers: List[QuotationExplorer[F]],
  quoteDao: QuoteDao[G]
)(implicit transaction: G ~> F)
    extends DataFeeder[F] {

  private val logger: LoggerF = Logger[DataFeederImpl[F, G]]

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
            .chunkN(10)
            .evalMap { chunks =>
                transaction {
                  chunks.toList.traverse { quote =>
                    quoteDao.insert(quote).map(_.fold(_ => 0, _ => 1))
                  }
                }
                  .map(_.sum)
            }
            .onFinalize(lockService.releaseLock(lock.id).productR(Applicative[F].unit))
            .groupWithin(20, 5 seconds)
            .scan(0) {
              case (count, chunk) => chunk.sumAll + count
            }
            .tail
            .evalMap { count => logger.infoF(s"Saved: $count quotes").as(DataFeedResults(count)) }
      }

  def discover(quotationExplorer: QuotationExplorer[F]): Stream[F, Quote] =
    quotationExplorer.discover
      .mapAsyncUnordered(100) { discoveredQuote =>
        for {
          id <- Random[F, UUID].generate
          timestamp <- Clock[F].realTime(TimeUnit.MILLISECONDS).map(milliseconds => new DateTime(milliseconds))

          authorHash <- hashingService.hash(discoveredQuote.author.getBytes)
          textHash <- hashingService.hash(discoveredQuote.text.getBytes)

        } yield Quote(id, timestamp, s"$authorHash-$textHash", discoveredQuote.author, discoveredQuote.text)
      }

}
