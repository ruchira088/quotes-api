package com.ruchij

import cats.effect.{Async, Clock, ContextShift, ExitCode, IO, IOApp, Resource}
import cats.implicits._
import com.ruchij.config.ServiceConfiguration
import com.ruchij.dao.doobie.DoobieTransactor
import com.ruchij.dao.quote.DoobieQuoteDao
import com.ruchij.migration.MigrationApp
import com.ruchij.services.health.HealthServiceImpl
import com.ruchij.services.quote.QuotationServiceImpl
import com.ruchij.types.Random.randomUuid
import com.ruchij.web.Routes
import doobie.ConnectionIO
import org.http4s.HttpApp
import org.http4s.server.blaze.BlazeServerBuilder
import pureconfig.ConfigSource

import scala.concurrent.ExecutionContext

object App extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    for {
      configObjectSource <- IO.delay(ConfigSource.defaultApplication)
      serviceConfiguration <- ServiceConfiguration.parse[IO](configObjectSource)

      _ <-
        program[IO](serviceConfiguration)
          .use {
            httpApp =>
              BlazeServerBuilder.apply[IO](ExecutionContext.global)
                .withHttpApp(httpApp)
                .bindHttp(serviceConfiguration.httpConfiguration.port, serviceConfiguration.httpConfiguration.host)
                .serve.compile.drain
          }
    }
    yield ExitCode.Success

  def program[F[+ _]: Async: ContextShift: Clock](serviceConfiguration: ServiceConfiguration): Resource[F, HttpApp[F]] =
    Resource.eval(MigrationApp.migrate[F](serviceConfiguration.sqlDatabaseConfiguration))
      .productR {
        DoobieTransactor.create(serviceConfiguration.sqlDatabaseConfiguration)
          .map(_.trans)
          .map { implicit transactor =>
            val quotationService = new QuotationServiceImpl[F, ConnectionIO](DoobieQuoteDao)
            val healthService = new HealthServiceImpl[F](serviceConfiguration.buildInformation)

            Routes(quotationService, healthService)
          }
      }

}
