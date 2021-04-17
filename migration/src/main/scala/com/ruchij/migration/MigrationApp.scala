package com.ruchij.migration

import cats.effect.{ExitCode, IO, IOApp, Sync}
import cats.implicits._
import com.ruchij.migration.config.DatabaseConfiguration
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import pureconfig.ConfigSource

object MigrationApp extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    for {
      configObjectSource <- IO.delay(ConfigSource.defaultApplication)
      databaseConfiguration <- IO.suspend(DatabaseConfiguration.load[IO](configObjectSource))

      result <- migrate[IO](databaseConfiguration)
      _ <- IO.delay(println(s"Migration result: $result"))
    }
    yield ExitCode.Success

  def migrate[F[_]: Sync](databaseConfiguration: DatabaseConfiguration): F[MigrateResult] =
    for {
      flyway <-
        Sync[F].delay {
          Flyway.configure()
            .dataSource(databaseConfiguration.url, databaseConfiguration.user, databaseConfiguration.password)
            .load()
        }

      migrationResult <- Sync[F].delay(flyway.migrate())
    }
    yield migrationResult

}
