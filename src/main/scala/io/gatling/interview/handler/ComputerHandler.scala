package io.gatling.interview.handler

import cats.effect.Sync
import cats.implicits._
import io.gatling.interview.command.{Add, ComputerCommand, ListComputers, Show}
import io.gatling.interview.console.Console
import io.gatling.interview.model.Computer
import io.gatling.interview.repository.ComputerRepository

class ComputerHandler[F[_]](computerRepository: ComputerRepository[F], console: Console[F])(implicit
    F: Sync[F]
) {

  def handle(command: ComputerCommand): F[Unit] =
    command match {
      case ListComputers =>
        for {
          computers <- computerRepository.fetchAll()
          output = computers
            .map(toString)
            .map(s => s" - $s")
            .mkString("\n")
          _ <- console.println(output)
        } yield ()
      case Add(name, introduced, discontinued) =>
        for {
          computers <- computerRepository.fetchAll()
          nextId = computers.map(_.id).maxOption.map(_ + 1).getOrElse(1L)
          _ <- computerRepository.insert(
            Computer(
              id = nextId,
              name = name,
              introduced = introduced,
              discontinued = discontinued
            )
          )
        } yield ()
      case Show(id) =>
        for {
          maybeComputer <- computerRepository.fetch(id)
          output = maybeComputer.map(toString).getOrElse(s"no computer found for ID ${id.toString}")
          _ <- console.println(output)
        } yield ()
    }

  private def toString(c: Computer) = {
    val introduced = c.introduced.map(d => s", introduced: ${d.toString}").getOrElse("")
    val discontinued = c.discontinued.map(d => s", discontinued: ${d.toString}").getOrElse("")
    s"[${c.id.toString}] ${c.name}$introduced$discontinued"
  }
}
