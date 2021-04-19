package com.ruchij.test

import cats.effect.{ConcurrentEffect, ContextShift, Resource, Timer}
import com.ruchij.App
import com.ruchij.config.{BuildInformation, HttpConfiguration, ServiceConfiguration}
import com.ruchij.migration.config.DatabaseConfiguration
import com.ruchij.types.Random
import org.http4s.HttpApp

import java.util.UUID

object HttpTestApp {
  val BuildInfo: BuildInformation = BuildInformation(Some("test-branch"), Some("my-commit"), None)

  val HttpConfig: HttpConfiguration = HttpConfiguration("localhost", 80)

  def databaseConfiguration(name: String): DatabaseConfiguration =
    DatabaseConfiguration(s"jdbc:h2:mem:$name;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false", "", "")

  def create[F[+ _]: ConcurrentEffect: ContextShift: Timer]: Resource[F, (ServiceConfiguration, HttpApp[F])] =
    Resource.eval(Random[F, UUID].generate).flatMap { uuid =>
      val serviceConfiguration =
        ServiceConfiguration(
          databaseConfiguration(s"quotes-api-${uuid.toString.take(8)}"),
          HttpConfig,
          BuildInfo
        )

      App.program(serviceConfiguration).map(serviceConfiguration -> _)
    }
}
