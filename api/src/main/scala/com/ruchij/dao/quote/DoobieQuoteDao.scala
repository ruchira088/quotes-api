package com.ruchij.dao.quote

import com.ruchij.dao.doobie.DoobieMappings._
import com.ruchij.dao.quote.models.{Paging, Quote, SortBy}
import doobie.ConnectionIO
import doobie.implicits.toSqlInterpolator
import doobie.util.fragment.Fragment
import doobie.util.fragments.whereAndOpt

import java.util.UUID

object DoobieQuoteDao extends QuoteDao[ConnectionIO] {
  override type InsertionResult = Int

  val SelectQuery = fr"SELECT id, created_at, author, text FROM quote"

  override def insert(quote: Quote): ConnectionIO[Int] =
    sql"""
      INSERT INTO quote (id, created_at, author, text)
        VALUES (${quote.id}, ${quote.createdAt}, ${quote.author}, ${quote.text})
    """
      .update
      .run

  override def findById(id: UUID): ConnectionIO[Option[Quote]] =
    (SelectQuery ++ fr"WHERE id = $id")
      .query[Quote]
      .option

  override def find(maybeAuthor: Option[String], maybeText: Option[String], paging: Paging): ConnectionIO[Seq[Quote]] =
    (SelectQuery ++
        whereAndOpt(
          maybeAuthor.map(author => fr"author LIKE ${"%" + author + "%"}"),
          maybeText.map(text => fr"text LIKE ${"%" + text + "%"}")
        ) ++
      sortingAndOrdering(paging)
      )
      .query[Quote]
      .to[Seq]

  private def sortingAndOrdering(paging: Paging): Fragment =
    fr"ORDER BY ${sortByColumn(paging.sortBy)} ${paging.sortOrder.shortName} LIMIT ${paging.pageSize} OFFSET ${paging.pageNumber * paging.pageSize}"

  private val sortByColumn: SortBy => String = {
    case SortBy.CreationDate => "created_at"

    case value => value.entryName.toLowerCase
  }
}
