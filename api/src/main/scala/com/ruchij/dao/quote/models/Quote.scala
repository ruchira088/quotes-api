package com.ruchij.dao.quote.models

import org.joda.time.DateTime

import java.util.UUID

case class Quote(id: UUID, createdAt: DateTime, hash: String, author: String, text: String)
