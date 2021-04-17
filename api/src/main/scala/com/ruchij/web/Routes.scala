package com.ruchij.web

import cats.effect.Sync
import com.ruchij.services.health.HealthService
import com.ruchij.services.quote.QuotationService
import com.ruchij.web.middleware.{ExceptionHandler, NotFoundHandler}
import com.ruchij.web.routes.{HealthRoutes, QuoteRoutes}
import org.http4s.{HttpApp, HttpRoutes}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

object Routes {
  def apply[F[_]: Sync](quotationService: QuotationService[F], healthService: HealthService[F]): HttpApp[F] = {
    implicit val dsl: Http4sDsl[F] = new Http4sDsl[F] {}

    val routes: HttpRoutes[F] =
      Router(
        "/service" -> HealthRoutes(healthService),
        "/quote" -> QuoteRoutes(quotationService)
      )

    ExceptionHandler {
      NotFoundHandler(routes)
    }
  }
}
