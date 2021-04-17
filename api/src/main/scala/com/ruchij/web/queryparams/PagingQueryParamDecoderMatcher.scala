package com.ruchij.web.queryparams

import cats.data.{Kleisli, NonEmptyList}
import cats.implicits._
import com.ruchij.dao.quote.models.Paging.Default
import com.ruchij.dao.quote.models.{Paging, SortBy, SortOrder}
import org.http4s._

object PagingQueryParamDecoderMatcher {
  type QueryParameterMap = Map[String, collection.Seq[String]]

  case object PageSizeQueryParam extends QueryParam[Int] {
    override def key: QueryParameterKey = QueryParameterKey("page-size")
  }

  case object PageNumberQueryParam extends QueryParam[Int] {
    override def key: QueryParameterKey = QueryParameterKey("page-number")
  }

  case object SortByQueryParam extends QueryParam[SortBy] {
    override def key: QueryParameterKey = QueryParameterKey("sort-by")
  }

  case object SortOrderQueryParam extends QueryParam[SortOrder] {
    override def key: QueryParameterKey = QueryParameterKey("order")
  }

  implicit val sortByQueryParamDecoder: QueryParamDecoder[SortBy] =
    (queryParameterValue: QueryParameterValue) => SortBy.values.find(_.entryName.equalsIgnoreCase(queryParameterValue.value))
      .toValidNel(ParseFailure("Unable to parse as sort-by value", s"${queryParameterValue.value} is not a valid sort-by value"))

  implicit val sortOrderQueryParamDecoder: QueryParamDecoder[SortOrder] =
    (queryParameterValue: QueryParameterValue) => SortOrder.values.find(_.shortName.equalsIgnoreCase(queryParameterValue.value))
      .toValidNel(ParseFailure("Unable to parse as order value", s"${queryParameterValue.value} is not a valid order value"))

  def retrieve[A: QueryParamDecoder](queryParam: QueryParam[A], default: => A): Kleisli[Either[NonEmptyList[ParseFailure], *], QueryParameterMap, A] =
    Kleisli {
      parameterMap =>
        parameterMap.get(queryParam.key.value)
          .flatMap(_.headOption)
          .fold[Either[NonEmptyList[ParseFailure], A]](Right(default)) { string =>
            QueryParamDecoder[A].decode(QueryParameterValue(string)).toEither
          }
    }

  val parse: Kleisli[Either[NonEmptyList[ParseFailure], *], QueryParameterMap, Paging] =
    for {
      pageSize <- retrieve(PageSizeQueryParam, Default.pageSize)
      pageNumber <- retrieve(PageNumberQueryParam, Default.pageNumber)
      sortBy <- retrieve(SortByQueryParam, Default.sortBy)
      sortOrder <- retrieve(SortOrderQueryParam, Default.sortOrder)
    }
    yield Paging(pageSize, pageNumber, sortBy, sortOrder)

  def unapply(queryParameterMap: QueryParameterMap): Option[Either[ParseFailure, Paging]] =
    Some(parse.run(queryParameterMap).left.map(_.head))

}
