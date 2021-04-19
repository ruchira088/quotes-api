package com.ruchij.web.routes

import cats.effect.Sync
import cats.implicits._
import com.ruchij.circe.Decoders._
import com.ruchij.circe.Encoders._
import com.ruchij.services.quote.QuotationService
import com.ruchij.types.FunctionKTypes
import com.ruchij.web.queryparams.{AuthorQueryParamDecoderMatcher, PagingQueryParamDecoderMatcher, TextQueryParamDecoderMatcher}
import com.ruchij.web.requests.CreateQuotationRequest
import com.ruchij.web.responses.SearchResult
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

object QuoteRoutes {
  def apply[F[_]: Sync](quotationService: QuotationService[F])(implicit dsl: Http4sDsl[F]): HttpRoutes[F] = {
    import dsl._

    HttpRoutes.of {
      case request @ POST -> Root =>
        for {
          CreateQuotationRequest(author, text) <- request.as[CreateQuotationRequest]

          quoteEither <- quotationService.insert(author, text)

          response <- quoteEither.fold(existingQuote => Ok(existingQuote), createdQuote => Created(createdQuote))
        }
        yield response

      case GET -> Root :? PagingQueryParamDecoderMatcher(pagingResult) +& AuthorQueryParamDecoderMatcher(maybeAuthor) +& TextQueryParamDecoderMatcher(maybeText) =>
        for {
          paging <- FunctionKTypes.eitherToF[Throwable, F].apply(pagingResult)

          results <- quotationService.find(maybeAuthor, maybeText, paging)

          response <- Ok {
            SearchResult(
              maybeAuthor,
              maybeText,
              paging.pageSize,
              paging.pageNumber,
              paging.sortBy,
              paging.sortOrder,
              results
            )
          }
        }
        yield response

      case GET -> Root / UUIDVar(quoteId) =>
        quotationService.findById(quoteId).flatMap(quote => Ok(quote))

    }
  }
}
