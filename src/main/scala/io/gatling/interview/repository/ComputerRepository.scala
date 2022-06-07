package io.gatling.interview.repository
import io.gatling.interview.model.Computer

trait ComputerRepository[F[_]] {

  def fetchAll(): F[Seq[Computer]]

  def fetch(id: Long): F[Option[Computer]]

  def insert(computer: Computer): F[Unit]
}
