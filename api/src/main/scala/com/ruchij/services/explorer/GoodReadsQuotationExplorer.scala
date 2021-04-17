package com.ruchij.services.explorer

import cats.effect.Sync
import cats.implicits._
import com.ruchij.services.explorer.models.DiscoveredQuote
import fs2.Stream
import org.http4s.client.Client
import org.http4s.implicits.http4sLiteralsSyntax
import org.http4s.{ParseResult, Uri}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.jdk.CollectionConverters._

class GoodReadsQuotationExplorer[F[_]: Sync](client: Client[F]) extends QuotationExplorer[F] {

  override val discover: Stream[F, DiscoveredQuote] =
    Stream.eval(run(uri"/quotes"))
      .flatMap {
        document =>
          Stream.fromEither[F].apply(GoodReadsQuotationExplorer.tags(document))
            .flatMap { uris => Stream.emits[F, Uri](uris) }
      }
      .flatMap(readAllQuotes)


  def readAllQuotes(pageUri: Uri): Stream[F, DiscoveredQuote] =
    Stream.eval(run(pageUri))
      .flatMap { document =>
        val quotes: Seq[DiscoveredQuote] = GoodReadsQuotationExplorer.parseQuotes(document)
        val nextPage: ParseResult[Option[Uri]] = GoodReadsQuotationExplorer.nextPage(document)

        Stream.emits(quotes) ++
          nextPage.fold[Stream[F, DiscoveredQuote]](
            error => Stream.raiseError[F](error),
            _.fold[Stream[F, DiscoveredQuote]](Stream.empty)(readAllQuotes)
          )
      }

  def run(uri: Uri): F[Document] =
    client.expect[String](GoodReadsQuotationExplorer.Host.resolve(uri))
      .flatMap { html =>
        Sync[F].delay(Jsoup.parse(html))
      }

}

object GoodReadsQuotationExplorer {
  val Host: Uri = uri"https://www.goodreads.com"

  def parseQuotes(document: Document): Seq[DiscoveredQuote] =
    document.select(".quoteText").asScala.toSeq
      .map {
        quoteText =>
          val author = quoteText.select("span.authorOrTitle").text().filter(_ != ',')
          val text = quoteText.text().takeWhile(_ != '―').trim.filter(_.toInt < 128)

          DiscoveredQuote(author, text)
      }

  def nextPage(document: Document): ParseResult[Option[Uri]] =
    Option(document.select("a.next_page").first())
      .map(_.attr("href"))
      .filter(_.nonEmpty)
      .fold[ParseResult[Option[Uri]]](Right(None)) { href => Uri.fromString(href).map(Some.apply) }

  def tags(document: Document): ParseResult[List[Uri]] =
    document.select(".bigBox li a.gr-hyperlink").asScala.toList
      .map(_.attr("href"))
      .traverse(Uri.fromString)

}