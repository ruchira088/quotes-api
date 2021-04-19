package com.ruchij.test.utils

import java.util.concurrent.TimeUnit
import cats.effect.{Clock, ContextShift, IO, Sync, Timer}
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{FiniteDuration, TimeUnit}

object Providers {
  implicit def clock[F[_]: Sync]: Clock[F] = stubClock(DateTime.now())

  def stubClock[F[_]: Sync](dateTime: => DateTime): Clock[F] = new Clock[F] {
    override def realTime(unit: TimeUnit): F[Long] =
      Sync[F].delay(unit.convert(dateTime.getMillis, TimeUnit.MILLISECONDS))

    override def monotonic(unit: TimeUnit): F[Long] = realTime(unit)
  }

  implicit def contextShiftIO(implicit executionContext: ExecutionContext): ContextShift[IO] =
    IO.contextShift(executionContext)

  implicit def timer(implicit clockIO: Clock[IO], executionContext: ExecutionContext): Timer[IO] =
    new Timer[IO] {
      override def clock: Clock[IO] = clockIO

      override def sleep(duration: FiniteDuration): IO[Unit] = IO.timer(executionContext).sleep(duration)
    }
}
