package com.ruchij.dao.quote

import com.ruchij.dao.doobie.DoobieMappings._
import com.ruchij.dao.quote.models.{Paging, Quote, SortBy, SortOrder}
import doobie.ConnectionIO
import doobie.implicits.toSqlInterpolator
import doobie.util.fragment.Fragment
import doobie.util.fragments.whereAndOpt

import java.util.UUID

object DoobieQuoteDao extends QuoteDao[ConnectionIO] {

  val SelectQuery = fr"SELECT id, created_at, hash, author, text FROM quote"

  override def insert(quote: Quote): ConnectionIO[Int] =
    sql"""
      INSERT INTO quote (id, created_at, hash, author, text)
        VALUES (${quote.id}, ${quote.createdAt}, ${quote.hash}, ${quote.author}, ${quote.text})
    """
      .update
      .run

  override def findById(id: UUID): ConnectionIO[Option[Quote]] =
    (SelectQuery ++ fr"WHERE id = $id")
      .query[Quote]
      .option

  override def findByHash(hash: String): ConnectionIO[Option[Quote]] =
    (SelectQuery ++ fr"WHERE hash = $hash")
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
    fr"""
      ORDER BY ${sortByColumn(paging.sortBy)} ${sortOrder(paging.sortOrder)}
        LIMIT ${paging.pageSize} OFFSET ${paging.pageNumber * paging.pageSize}
    """

  private val sortByColumn: SortBy => Fragment = {
    case SortBy.CreationDate => fr"created_at"
    case SortBy.Author => fr"author"
    case SortBy.Text => fr"text"
  }

  private val sortOrder: SortOrder => Fragment = {
    case SortOrder.Ascending => fr"ASC"
    case SortOrder.Descending => fr"DESC"
  }
}
