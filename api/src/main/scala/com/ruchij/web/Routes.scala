package com.ruchij.web

import cats.effect.{Concurrent, Timer}
import com.ruchij.services.feeder.DataFeeder
import com.ruchij.services.health.HealthService
import com.ruchij.services.quote.QuotationService
import com.ruchij.web.middleware.{ExceptionHandler, NotFoundHandler}
import com.ruchij.web.routes.{DataFeederRoutes, HealthRoutes, QuoteRoutes}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.{HttpApp, HttpRoutes}

object Routes {
  def apply[F[_]: Timer: Concurrent](
    quotationService: QuotationService[F],
    dataFeeder: DataFeeder[F],
    healthService: HealthService[F]
  ): HttpApp[F] = {
    implicit val dsl: Http4sDsl[F] = new Http4sDsl[F] {}

    val routes: HttpRoutes[F] =
      Router(
        "/service" -> HealthRoutes(healthService),
        "/quote" -> QuoteRoutes(quotationService),
        "/data-feeder" -> DataFeederRoutes(dataFeeder)
      )

    ExceptionHandler {
      NotFoundHandler(routes)
    }
  }
}
