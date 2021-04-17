package com.ruchij.circe

import enumeratum.EnumEntry
import io.circe.Encoder
import org.joda.time.DateTime

object Encoders {
  implicit val dateTimeEncoder: Encoder[DateTime] = Encoder.encodeString.contramap[DateTime](_.toString)

  implicit def enumEncoder[A <: EnumEntry]: Encoder[A] = Encoder.encodeString.contramap[A](_.entryName)
}
