package com.ruchij.services.feeder.models

sealed trait FeederResult

object FeederResult {
  case class DataFeedResults(count: Int) extends FeederResult

  case object ExistingDataFeed extends FeederResult
}
