package com.ruchij.dao.doobie

import enumeratum.{Enum, EnumEntry}
import org.{h2, postgresql}

import java.sql.Driver
import scala.reflect.ClassTag
import scala.util.matching.Regex

sealed abstract class DatabaseDriver[A <: Driver](implicit classTag: ClassTag[A]) extends EnumEntry {
  val clazz: Class[_] = classTag.runtimeClass
}

object DatabaseDriver extends Enum[DatabaseDriver[_]] {
  val DbShortName: Regex = "jdbc:([^:]+):.*".r

  case object H2 extends DatabaseDriver[h2.Driver]

  case object Postgres extends DatabaseDriver[postgresql.Driver]

  val infer: String => Either[IllegalArgumentException, DatabaseDriver[_]] = {
    case DbShortName(name) =>
      values.find(_.entryName.equalsIgnoreCase(name))
        .fold[Either[IllegalArgumentException, DatabaseDriver[_]]](Left(new IllegalArgumentException(s"Unable to find database driver for $name")))(Right.apply)

    case url => Left(new IllegalArgumentException(s"Unable to extract short name from $url"))
  }

  override def values: IndexedSeq[DatabaseDriver[_]] = findValues
}
