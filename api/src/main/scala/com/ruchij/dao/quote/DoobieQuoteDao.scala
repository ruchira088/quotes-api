package com.ruchij.dao.quote

import com.ruchij.dao.doobie.DoobieMappings._
import com.ruchij.dao.quote.models.{Quote, SortBy}
import doobie.ConnectionIO
import doobie.implicits.toSqlInterpolator

import java.util.UUID

object DoobieQuoteDao extends QuoteDao[ConnectionIO] {
  override type InsertionResult = Int

  override def insert(quote: Quote): ConnectionIO[Int] =
    sql"""
      INSERT INTO quote (id, created_at, author, text)
        VALUES (${quote.id}, ${quote.createdAt}, ${quote.author}, ${quote.text})
    """
      .update
      .run

  override def retrieveAll(sortBy: SortBy, size: Int, offset: Int): ConnectionIO[Seq[Quote]] =
    sql"SELECT id, created_at, author, text FROM quote ORDER BY ${sortBy} LIMIT $size OFFSET $offset"
      .query[Quote]
      .to[Seq]

  override def findById(id: UUID): ConnectionIO[Option[Quote]] =
    sql"SELECT id, created_at, author, text FROM quote WHERE id = $id"
      .query[Quote]
      .option

  override def searchByAuthor(author: String, size: Int, offset: Int): ConnectionIO[Seq[Quote]] =
    sql"""
      SELECT id, created_at, author, text FROM quote
        WHERE author LIKE ${"%" + author + "%"} LIMIT $size OFFSET $offset
    """
      .query[Quote]
      .to[Seq]

  override def searchByText(text: String, size: Int, offset: Int): ConnectionIO[Seq[Quote]] =
    sql"""
      SELECT id, created_at, author, text FROM quote
        WHERE text LIKE ${"%" + text + "%"} LIMIT $size OFFSET $offset
    """
      .query[Quote]
      .to[Seq]

  val sortByColumn: SortBy => String = {
    case SortBy.Author => "author"

    case SortBy.Text => "text"

    case SortBy.CreationDate => "created_at"
  }
}
