package com.ruchij.dao.quote.models

import org.joda.time.DateTime

import java.util.UUID

case class Quote(id: UUID, createdAt: DateTime, author: String, text: String)
