package com.ruchij.services.hash

import cats.effect.{Blocker, ContextShift, Sync}

import scala.util.hashing.MurmurHash3

class Murmur3HashService[F[_]: Sync: ContextShift](blocker: Blocker) extends HashingService[F] {

  override def hash(value: Array[Byte]): F[String] =
    blocker.delay {
      Integer.toHexString(MurmurHash3.bytesHash(value))
    }

}
