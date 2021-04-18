package com.ruchij.services.feeder

import com.ruchij.services.feeder.models.FeederResult
import fs2.Stream

trait DataFeeder[F[_]] {
  val run: Stream[F, FeederResult]
}