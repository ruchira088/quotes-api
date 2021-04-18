package com.ruchij.web.routes

import cats.effect.{Concurrent, Timer}
import com.ruchij.services.feeder.DataFeeder
import com.ruchij.services.feeder.models.FeederResult
import com.ruchij.services.feeder.models.FeederResult.DataFeedResults
import com.ruchij.web.responses.SseTopic
import fs2.Stream
import io.circe.Encoder
import io.circe.generic.auto._
import io.circe.literal.JsonStringContext
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, ServerSentEvent}

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object DataFeederRoutes {
  def apply[F[_]: Concurrent: Timer](dataFeeder: DataFeeder[F])(implicit dsl: Http4sDsl[F]): HttpRoutes[F] = {
    import dsl._

    HttpRoutes.of {
      case POST -> Root =>
        Ok {
          dataFeeder.run
            .map {
              case dataFeedResults: FeederResult.DataFeedResults =>
                ServerSentEvent(
                  Encoder[DataFeedResults].apply(dataFeedResults).noSpaces,
                  Some(SseTopic.FeedProgress.entryName)
                )

              case FeederResult.ExistingDataFeed =>
                ServerSentEvent(json"""{ "existingFeed": true }""".noSpaces, Some(SseTopic.ExistingFeed.entryName))
            }
            .merge {
              Stream.awakeEvery[F](10 seconds).map { duration =>
                ServerSentEvent(
                  json"""{ "duration": ${duration.toSeconds} }""".noSpaces,
                  Some(SseTopic.HeartBeat.entryName)
                )
              }
            }
        }
    }
  }
}
