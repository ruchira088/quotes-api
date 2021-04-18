package com.ruchij.services.explorer

import cats.effect.{Concurrent, Sync}
import cats.implicits._
import com.ruchij.services.explorer.models.DiscoveredQuote
import com.ruchij.syntax.toLoggerF
import com.ruchij.types.LoggerF
import com.typesafe.scalalogging.Logger
import fs2.Stream
import org.http4s.client.Client
import org.http4s.implicits.http4sLiteralsSyntax
import org.http4s.{ParseResult, Uri}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.jdk.CollectionConverters._

class GoodReadsQuotationExplorer[F[_]: Concurrent](client: Client[F]) extends QuotationExplorer[F] {

  private val logger: LoggerF = Logger[GoodReadsQuotationExplorer[F]]

  override val discover: Stream[F, DiscoveredQuote] = {
    Stream.eval(logger.infoF("Starting discovery..."))
      .productR(Stream.eval(run(uri"/quotes")))
      .flatMap {
        document =>
          Stream.fromEither[F].apply(GoodReadsQuotationExplorer.tags(document))
            .flatMap {
              _.foldLeft[Stream[F, DiscoveredQuote]](Stream.empty) {
                case (stream, (name, uri)) =>
                  Stream.eval(logger.infoF(s"Discovering quotes about $name"))
                    .productR(stream.merge(readAllQuotes(name, uri)))
              }
            }
      }
  }

  def readAllQuotes(name: String, pageUri: Uri): Stream[F, DiscoveredQuote] =
    Stream.eval(run(pageUri))
      .flatMap { document =>
        val quotes: Seq[DiscoveredQuote] = GoodReadsQuotationExplorer.parseQuotes(document)
        val nextPage: ParseResult[Option[Uri]] = GoodReadsQuotationExplorer.nextPage(document)

        Stream.emits(quotes) ++
          nextPage.fold[Stream[F, DiscoveredQuote]](
            error => Stream.raiseError[F](error),
            _.fold[Stream[F, DiscoveredQuote]](Stream.eval(logger.infoF(s"Finished discovery for $name")).productR(Stream.empty)) {
              uri => readAllQuotes(name, uri)
            }
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

  def tags(document: Document): ParseResult[List[(String, Uri)]] =
    document.select(".bigBox li a.gr-hyperlink").asScala.toList
      .map { element => element.text() -> element.attr("href") }
      .traverse {
        case (name, uriString) =>
          Uri.fromString(uriString).map(name -> _)
      }

}