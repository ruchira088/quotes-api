package com.ruchij.web.queryparams

import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher

case object TextQueryParamDecoderMatcher extends OptionalQueryParamDecoderMatcher[String]("text")
