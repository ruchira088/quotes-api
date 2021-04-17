package com.ruchij.services.explorer

import com.ruchij.services.explorer.models.DiscoveredQuote
import fs2.Stream

trait QuotationExplorer[F[_]] {
  val discover: Stream[F, DiscoveredQuote]
}
