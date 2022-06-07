package io.gatling.interview.command

import java.time.LocalDate
import scala.util.Try

object ComputerCommand {
  def parse(args: List[String]): Option[ComputerCommand] =
    args match {
      case "list" :: _ => Some(ListComputers)
      case "add" :: "-n" :: name :: "-i" :: introducedArg :: "-d" :: discontinuedArg :: _ =>
        for {
          introduced <- Try(LocalDate.parse(introducedArg)).toOption
          discontinued <- Try(LocalDate.parse(discontinuedArg)).toOption
        } yield Add(name, Some(introduced), Some(discontinued))
      case "add" :: "-n" :: name :: "-i" :: introducedArg :: _ =>
        for {
          introduced <- Try(LocalDate.parse(introducedArg)).toOption
        } yield Add(name, Some(introduced), None)
      case "add" :: "-n" :: name :: _ => Some(Add(name, None, None))
      case _                          => None
    }
}

sealed trait ComputerCommand

case object ListComputers extends ComputerCommand
case class Add(
    name: String,
    introduced: Option[LocalDate],
    discontinued: Option[LocalDate]
) extends ComputerCommand
