package com.ruchij.dao.doobie

import doobie.implicits.javasql.TimestampMeta
import doobie.util.{Get, Put}
import org.joda.time.DateTime

import java.sql.Timestamp
import java.util.UUID
import scala.util.Try

object DoobieMappings {
  implicit val uuidPut: Put[UUID] = Put[String].tcontramap[UUID](_.toString)

  implicit val uuidGet: Get[UUID] =
    Get[String].temap(string => Try(UUID.fromString(string)).toEither.left.map(_.getMessage))

  implicit val dateTimePut: Put[DateTime] =
    Put[Timestamp].tcontramap[DateTime](dateTime => new Timestamp(dateTime.getMillis))

  implicit val dateTimeGet: Get[DateTime] =
    Get[Timestamp].tmap(timestamp => new DateTime(timestamp))

}
