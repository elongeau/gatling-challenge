package io.gatling.interview.repository

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{Blocker, IO, Resource}
import io.circe.ParsingFailure
import io.circe.jawn.decode
import io.gatling.interview.model.Computer
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

import java.io.File
import java.nio.file.{Files, Path, StandardCopyOption}
import java.time.{LocalDate, Month}
import java.util.UUID
import scala.io.Source

class FileComputerRepositorySpec extends AsyncWordSpec with AsyncIOSpec with Matchers {

  private val blocker = Blocker.liftExecutionContext(executionContext)

  "Fetching all computers from file" should {
    "retrieve all computers" in {
      val expectedComputers = Seq(
        Computer(id = 1, name = "MacBook Pro 15.4 inch", introduced = None, discontinued = None),
        Computer(
          id = 2,
          name = "CM-5",
          introduced = Some(LocalDate.of(1991, Month.JANUARY, 1)),
          discontinued = None
        ),
        Computer(
          id = 3,
          name = "Apple IIee",
          introduced = Some(LocalDate.of(2006, Month.JANUARY, 10)),
          discontinued = Some(LocalDate.of(2010, Month.JANUARY, 10))
        )
      )

      temporaryFileResource("computers/computers.json")
        .use { computersFilePath =>
          val repository = new FileComputerRepository[IO](computersFilePath, blocker)
          repository.fetchAll()
        }
        .asserting { fetchedComputers =>
          fetchedComputers shouldBe expectedComputers
        }
    }
  }

  "fail if the JSON file is invalid" in {
    temporaryFileResource("computers/computers-invalid.json")
      .use { computersFilePath =>
        val repository = new FileComputerRepository[IO](computersFilePath, blocker)
        repository.fetchAll()
      }
      .assertThrows[ParsingFailure]
  }

  "Fetching a single computer from file" should {
    "return computer if it exists" in {
      temporaryFileResource("computers/computers.json")
        .use { computersFilePath =>
          val repository = new FileComputerRepository[IO](computersFilePath, blocker)
          repository.fetch(1)
        }
        .asserting { fetchedComputer =>
          fetchedComputer shouldBe Some(
            Computer(id = 1, name = "MacBook Pro 15.4 inch", introduced = None, discontinued = None)
          )
        }

    }

    "return none if not found" in {
      temporaryFileResource("computers/computers.json")
        .use { computersFilePath =>
          val repository = new FileComputerRepository[IO](computersFilePath, blocker)
          repository.fetch(4)
        }
        .asserting { fetchedComputer =>
          fetchedComputer shouldBe None
        }
    }

    "Insert a new computer" should {
      "append computer to file" in {
        val newComputer = Computer(4, "mac", None, None)
        temporaryFileResource("computers/computers.json")
          .use { computersFilePath =>
            val repository = new FileComputerRepository[IO](computersFilePath, blocker)
            repository.insert(newComputer) *> IO.delay(
              Source.fromFile(computersFilePath.toString).mkString
            )
          }
          .asserting { json =>
//            val json = Source.fromFile(path.toString).mkString
            val computers = decode[Seq[Computer]](json)
            computers shouldBe Right(
              Seq(
                Computer(
                  id = 1,
                  name = "MacBook Pro 15.4 inch",
                  introduced = None,
                  discontinued = None
                ),
                Computer(
                  id = 2,
                  name = "CM-5",
                  introduced = Some(LocalDate.of(1991, Month.JANUARY, 1)),
                  discontinued = None
                ),
                Computer(
                  id = 3,
                  name = "Apple IIee",
                  introduced = Some(LocalDate.of(2006, Month.JANUARY, 10)),
                  discontinued = Some(LocalDate.of(2010, Month.JANUARY, 10))
                ),
                newComputer
              )
            )
          }
      }
    }

  }

  private def temporaryFileResource(path: String): Resource[IO, Path] =
    Resource(
      for {
        inputStream <- IO.delay(getClass.getClassLoader.getResourceAsStream(path))
        file <- IO.delay(File.createTempFile(UUID.randomUUID().toString, "tmp"))
        path = file.toPath
        _ <- IO.delay(file.deleteOnExit())
        _ <- IO.delay(Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING))
      } yield (path, IO.delay(file.delete()).as(()))
    )
}
