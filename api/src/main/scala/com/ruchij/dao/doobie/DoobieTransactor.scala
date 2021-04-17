package com.ruchij.dao.doobie

import cats.effect.{Async, Blocker, ContextShift, Resource}
import com.ruchij.config.DoobieDatabaseConfiguration
import com.ruchij.types.FunctionKTypes
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts

object DoobieTransactor {

  def create[F[_] : Async : ContextShift](doobieDatabaseConfiguration: DoobieDatabaseConfiguration): Resource[F, HikariTransactor[F]] =
    for {
      databaseDriver <-
        Resource.eval(FunctionKTypes.eitherToF[Throwable, F].apply(DatabaseDriver.infer(doobieDatabaseConfiguration.url)))

      connectEC <- ExecutionContexts.fixedThreadPool(4)
      blockingIO <- ExecutionContexts.cachedThreadPool

      transactor <-
        HikariTransactor.newHikariTransactor[F](
          databaseDriver.clazz.getName,
          doobieDatabaseConfiguration.url,
          doobieDatabaseConfiguration.user,
          doobieDatabaseConfiguration.password,
          connectEC,
          Blocker.liftExecutionContext(blockingIO)
        )
    }
    yield transactor

}
