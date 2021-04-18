package com.ruchij.services.feeder

import com.ruchij.services.feeder.models.FeederResult

trait DataFeeder[F[_]] {
  val run: F[FeederResult]
}