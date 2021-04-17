package com.ruchij.types

import cats.effect.Sync

import java.util.UUID

trait Random[F[+ _], +A] {
  val generate: F[A]
}

object Random {
  def apply[F[+ _], A](implicit random: Random[F, A]): Random[F, A] = random

  implicit def randomUuid[F[+ _]: Sync]: Random[F, UUID] =
    new Random[F, UUID] {
      override val generate: F[UUID] = Sync[F].delay(UUID.randomUUID())
    }
}
