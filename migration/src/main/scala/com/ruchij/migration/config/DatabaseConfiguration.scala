package com.ruchij.migration.config

import cats.{Applicative, ApplicativeError}
import pureconfig.ConfigObjectSource
import pureconfig.error.ConfigReaderException
import pureconfig.generic.auto._

case class DatabaseConfiguration(url: String, user: String, password: String)

object DatabaseConfiguration {

  def load[F[_]: ApplicativeError[*[_], Throwable]](configObjectSource: ConfigObjectSource): F[DatabaseConfiguration] =
    configObjectSource.at("sql-database-configuration").load[DatabaseConfiguration]
      .fold(errors => ApplicativeError[F, Throwable].raiseError(ConfigReaderException(errors)), config => Applicative[F].pure(config))

}
