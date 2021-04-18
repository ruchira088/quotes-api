package com.ruchij.services.hash

trait HashingService[F[_]] {
  def hash(value: Array[Byte]): F[String]
}
