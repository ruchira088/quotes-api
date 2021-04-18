package com.ruchij

import cats.effect.{ConcurrentEffect, ContextShift, ExitCode, IO, IOApp, Resource, Timer}
import cats.implicits._
import com.ruchij.config.ServiceConfiguration
import com.ruchij.dao.doobie.DoobieTransactor
import com.ruchij.dao.lock.DoobieLockDao
import com.ruchij.dao.quote.DoobieQuoteDao
import com.ruchij.migration.MigrationApp
import com.ruchij.services.explorer.QuotationExplorer
import com.ruchij.services.feeder.DataFeederImpl
import com.ruchij.services.health.HealthServiceImpl
import com.ruchij.services.lock.LockServiceImpl
import com.ruchij.services.quote.QuotationServiceImpl
import com.ruchij.types.Random.randomUuid
import com.ruchij.web.Routes
import doobie.ConnectionIO
import org.http4s.HttpApp
import org.http4s.client.blaze.BlazeClientBuilder
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

  def program[F[+ _]: ConcurrentEffect: ContextShift: Timer](serviceConfiguration: ServiceConfiguration): Resource[F, HttpApp[F]] =
    Resource.eval(MigrationApp.migrate[F](serviceConfiguration.sqlDatabaseConfiguration))
      .productR {
        DoobieTransactor.create(serviceConfiguration.sqlDatabaseConfiguration)
          .map(_.trans)
          .flatMap { implicit transactor =>
            for {
              client <- BlazeClientBuilder.apply[F](ExecutionContext.global).resource

              quotationService = new QuotationServiceImpl[F, ConnectionIO](DoobieQuoteDao)
              healthService = new HealthServiceImpl[F](serviceConfiguration.buildInformation)
              lockService = new LockServiceImpl[F, ConnectionIO](DoobieLockDao)
              dataFeeder = new DataFeederImpl[F, ConnectionIO](lockService, QuotationExplorer.all(client), DoobieQuoteDao)
            }
            yield Routes(quotationService, dataFeeder, healthService)
          }
      }

}
