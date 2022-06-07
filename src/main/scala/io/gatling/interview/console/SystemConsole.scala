package io.gatling.interview.console

import cats.effect.Sync

class SystemConsole[F[_]](implicit F: Sync[F]) extends Console[F] {

  def println(s: String): F[Unit] = F.delay(scala.Console.println(s))
}
