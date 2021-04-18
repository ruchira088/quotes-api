package com.ruchij.types

import cats.effect.Sync
import com.typesafe.scalalogging.Logger

class LoggerF(val logger: Logger) extends AnyVal {

  def infoF[F[_]: Sync](message: String): F[Unit] =
    Sync[F].delay(logger.info(message))

  def errorF[F[_]: Sync](message: String, throwable: Throwable): F[Unit] =
    Sync[F].delay(logger.error(message, throwable))

}
