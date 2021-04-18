package com.ruchij.web.responses

import enumeratum.{Enum, EnumEntry}

sealed trait SseTopic extends EnumEntry

object SseTopic extends Enum[SseTopic] {
  case object FeedProgress extends SseTopic
  case object ExistingFeed extends SseTopic
  case object HeartBeat extends SseTopic

  override def values: IndexedSeq[SseTopic] = findValues
}
