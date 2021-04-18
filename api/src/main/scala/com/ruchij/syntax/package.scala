package com.ruchij

import cats.implicits._
import cats.{Applicative, ApplicativeError, MonadError}
import com.ruchij.types.LoggerF
import com.typesafe.scalalogging.Logger

import scala.language.implicitConversions

package object syntax {
  implicit class OptionWrapper[F[_], A](maybeValueF: F[Option[A]]) {
    def to[E](onEmpty: => E)(implicit monadError: MonadError[F, E]): F[A] =
      maybeValueF.flatMap {
        _.fold[F[A]](ApplicativeError[F, E].raiseError(onEmpty))(value => Applicative[F].pure(value))
      }
  }

  implicit def toLoggerF(logger: Logger): LoggerF = new LoggerF(logger)
}
