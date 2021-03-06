package com.ruchij.services.explorer

import cats.effect.Concurrent
import com.ruchij.services.explorer.models.DiscoveredQuote
import fs2.Stream
import org.http4s.client.Client

trait QuotationExplorer[F[_]] {
  val discover: Stream[F, DiscoveredQuote]
}

object QuotationExplorer {
  def all[F[_]: Concurrent](client: Client[F]): List[QuotationExplorer[F]] =
    List(new GoodReadsQuotationExplorer[F](client))
}