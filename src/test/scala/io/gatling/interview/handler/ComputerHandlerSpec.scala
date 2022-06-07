package io.gatling.interview.handler

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import io.gatling.interview.command.{Add, Show}
import io.gatling.interview.console.Console
import io.gatling.interview.model.Computer
import io.gatling.interview.repository.ComputerRepository
import org.scalatest.BeforeAndAfter
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

import java.time.LocalDate
import scala.collection.mutable.ListBuffer

class ComputerHandlerSpec extends AsyncWordSpec with AsyncIOSpec with Matchers with BeforeAndAfter {
  private class InMemoryConsole extends Console[IO] {
    val lines: ListBuffer[String] = ListBuffer.empty[String]
    override def println(s: String): IO[Unit] = IO.pure {
      lines += s
    }
    def clear(): Unit = lines.clear()
  }

  private class InMemoryComputerRepository extends ComputerRepository[IO] {
    val computers: ListBuffer[Computer] = ListBuffer.empty[Computer]
    override def fetchAll(): IO[Seq[Computer]] = IO.pure {
      computers.toSeq
    }

    override def fetch(id: Long): IO[Option[Computer]] = IO.pure {
      computers.find(_.id == id)
    }

    override def insert(computer: Computer): IO[Unit] = IO.pure {
      computers += computer
    }

    def clear(): Unit = computers.clear()
  }
  private val console = new InMemoryConsole
  private val repository = new InMemoryComputerRepository
  private val handler = new ComputerHandler[IO](repository, console)

  before {
    console.clear()
    repository.clear()
  }

  "handling Show command" should {
    "display computer on console" in {
      repository.insert(
        Computer(
          id = 1,
          name = "Mac",
          introduced = Some(LocalDate.parse("2021-01-01")),
          discontinued = Some(LocalDate.parse("2021-12-31"))
        )
      )

      val result = handler.handle(Show(1))

      result.asserting { _ =>
        console.lines shouldBe List("[1] Mac, introduced: 2021-01-01, discontinued: 2021-12-31")
      }
    }

    "inform there is no computer for ID" in {
      val result = handler.handle(Show(1))

      result.asserting { _ =>
        console.lines shouldBe List("no computer found for ID 1")
      }
    }
  }

  "handling Add command" should {
    "add computer" when {
      "there is no computer" in {
        val result = handler.handle(
          Add(
            name = "mac",
            introduced = Some(LocalDate.parse("2021-01-01")),
            discontinued = Some(LocalDate.parse("2021-12-31"))
          )
        )

        result.asserting { _ =>
          repository.computers.toList shouldBe List(
            Computer(
              id = 1,
              name = "mac",
              introduced = Some(LocalDate.parse("2021-01-01")),
              discontinued = Some(LocalDate.parse("2021-12-31"))
            )
          )
        }
      }

      "insert computer and increment max ID" in {
        val otherComputer = Computer(3, "Mac air", None, None)

        val result = repository.insert(otherComputer) *> handler.handle(
          Add("mac", None, None)
        )

        result.asserting { _ =>
          repository.computers.toList shouldBe List(otherComputer, Computer(4, "mac", None, None))
        }
      }
    }
  }

}
