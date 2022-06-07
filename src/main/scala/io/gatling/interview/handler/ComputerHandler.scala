package io.gatling.interview.handler

import cats.effect.Sync
import cats.implicits._
import io.gatling.interview.command.{Add, ComputerCommand, ListComputers, Show}
import io.gatling.interview.console.Console
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
            .map { c =>
              val introduced = c.introduced.map(d => s", introduced: ${d.toString}").getOrElse("")
              val discontinued =
                c.discontinued.map(d => s", discontinued: ${d.toString}").getOrElse("")
              s"- [${c.id.toString}] ${c.name}$introduced$discontinued"
            }
            .mkString("\n")
          _ <- console.println(output)
        } yield ()
      case Add(name, introduced, discontinued) => F.unit
      case Show(id)                            => F.unit
    }
}
