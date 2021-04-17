package com.ruchij.circe

import io.circe.Decoder
import org.joda.time.DateTime

import scala.util.Try

object Decoders {
  implicit val dateTimeDecoder: Decoder[DateTime] =
    Decoder.decodeString.emapTry(dateTimeString => Try(DateTime.parse(dateTimeString)))

  implicit val stringDecoder: Decoder[String] =
    Decoder.decodeString.emap(string => if (string.trim.isEmpty) Left("Cannot be empty") else Right(string))
}
