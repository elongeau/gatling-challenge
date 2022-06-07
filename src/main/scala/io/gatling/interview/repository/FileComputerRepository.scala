package io.gatling.interview.repository

import cats.effect.{Blocker, ContextShift, Sync}
import cats.implicits._
import io.circe.syntax._
import io.circe.parser.decode
import io.gatling.interview.model.Computer
import io.gatling.interview.repository.FileComputerRepository.ComputersFileCharset

import java.nio.charset.{Charset, StandardCharsets}
import java.nio.file.{Files, Path, Paths, StandardOpenOption}

object FileComputerRepository {
  val DefaultComputersFilePath: Path = Paths.get("computers.json")
  private val ComputersFileCharset: Charset = StandardCharsets.UTF_8
}

class FileComputerRepository[F[_]: ContextShift](filePath: Path, blocker: Blocker)(implicit
    F: Sync[F]
) extends ComputerRepository[F] {

  def fetchAll(): F[Seq[Computer]] =
    for {
      json <- blocker.blockOn(F.delay {
        val jsonBytes = Files.readAllBytes(filePath)
        new String(jsonBytes, ComputersFileCharset)
      })
      computers <- F.fromEither(decode[Seq[Computer]](json))
    } yield computers

  // a lot of improvement can be done here by taking the nth element of the JSON array before decoding it to a Computer
  def fetch(id: Long): F[Option[Computer]] =
    for {
      computers <- fetchAll()
    } yield computers.find(computer => computer.id == id)

  def insert(computer: Computer): F[Unit] = for {
    existingComputers <- fetchAll()
    computers = existingComputers :+ computer
    json = computers.asJson.toString()
    _ <- blocker.blockOn(F.delay {
      Files.write(
        filePath,
        json.getBytes(ComputersFileCharset),
        StandardOpenOption.TRUNCATE_EXISTING
      )
    })
  } yield ()
}
