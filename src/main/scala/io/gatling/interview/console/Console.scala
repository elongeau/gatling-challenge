package io.gatling.interview.console

trait Console[F[_]] {
  def println(s: String): F[Unit]
}
