package com.ruchij.web.routes

import cats.effect.Sync
import cats.implicits._
import com.ruchij.services.feeder.DataFeeder
import com.ruchij.services.feeder.models.FeederResult
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

object DataFeederRoutes {
  def apply[F[_]: Sync](dataFeeder: DataFeeder[F])(implicit dsl: Http4sDsl[F]): HttpRoutes[F] = {
    import dsl._

    HttpRoutes.of {
      case POST -> Root =>
        dataFeeder.run.flatMap {
          case FeederResult.DataFeedResults(count) =>

          case FeederResult.ExistingDataFeed =>
        }

    }
  }
}
