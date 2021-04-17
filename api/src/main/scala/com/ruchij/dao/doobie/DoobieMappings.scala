package com.ruchij.dao.doobie

import doobie.implicits.javasql.TimestampMeta
import doobie.util.{Get, Put}
import enumeratum.{Enum, EnumEntry}
import org.joda.time.DateTime

import java.sql.Timestamp
import java.util.UUID
import scala.reflect.ClassTag
import scala.util.Try

object DoobieMappings {
  implicit val uuidPut: Put[UUID] = Put[String].tcontramap[UUID](_.toString)

  implicit val uuidGet: Get[UUID] =
    Get[String].temap(string => Try(UUID.fromString(string)).toEither.left.map(_.getMessage))

  implicit val dateTimePut: Put[DateTime] =
    Put[Timestamp].tcontramap[DateTime](dateTime => new Timestamp(dateTime.getMillis))

  implicit val dateTimeGet: Get[DateTime] =
    Get[Timestamp].tmap(timestamp => new DateTime(timestamp))

  implicit def enumPut[A <: EnumEntry]: Put[A] = Put[String].tcontramap[A](_.entryName)

  implicit def enumGet[A <: EnumEntry](implicit enumValue: Enum[A], classTag: ClassTag[A]): Get[A] =
    Get[String].temap { string =>
      enumValue.values
        .find(_.entryName.equalsIgnoreCase(string))
        .fold[Either[String, A]](Left(s"Unable to convert $string to a type of ${classTag.runtimeClass.getName}"))(
          Right.apply[String, A]
        )
    }

}
