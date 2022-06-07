package io.gatling.interview.command

import java.time.LocalDate
import scala.util.Try

object ComputerCommand {
  def parse(args: List[String]): Option[ComputerCommand] =
    args match {
      case "list" :: _  => Some(ListComputers)
      case "add" :: Nil => None
      case "add" :: rest if rest.size > 6 || rest.size % 2 != 0 || !rest.contains("-n") => None
      case "add" :: rest =>
        val name = rest(rest.indexOf("-n") + 1)
        val introduced = parseDateArgument(rest, "-i")
        val discontinued = parseDateArgument(rest, "-d")
        Some(Add(name, introduced, discontinued))
      case "show" :: id :: _ => id.toLongOption.map(Show)
      case _                 => None
    }

  private def parseDateArgument(arguments: List[String], argument: String): Option[LocalDate] = {
    val argPosition = arguments.indexOf(argument)
    val rawArgument = arguments(argPosition + 1)
    Try(LocalDate.parse(rawArgument)).toOption
  }
}

sealed trait ComputerCommand

case object ListComputers extends ComputerCommand
case class Add(
    name: String,
    introduced: Option[LocalDate],
    discontinued: Option[LocalDate]
) extends ComputerCommand
case class Show(id: Long) extends ComputerCommand
