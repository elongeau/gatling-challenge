package io.gatling.interview.command

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.LocalDate

class ComputerCommandTest extends AnyWordSpec with Matchers {
  "parsing arguments from command line" should {
    "return no command when there is no arguments" in {
      ComputerCommand.parse(List.empty) shouldBe None
    }

    "return no command when argument is unknown" in {
      ComputerCommand.parse(List("foo")) shouldBe None
    }
  }

  """handling a "list" argument""" should {
    "returns a List command" when {
      """it has a single "list" arguments""" in {
        ComputerCommand.parse(List("list")) shouldBe Some(ListComputers)
      }

      """it has a "list" arguments and other arguments that are ignored""" in {
        ComputerCommand.parse(List("list", "foo")) shouldBe Some(ListComputers)
      }
    }
  }

  """handling an "add" argument""" should {
    "returns an Add Command" when {
      """it has an "add" and a computer name arguments""" in {
        ComputerCommand.parse(List("add", "-n", "Mac")) shouldBe Some(Add("Mac", None, None))
      }

      """it has an "add", a computer name and an introduced date arguments""" in {
        ComputerCommand.parse(List("add", "-n", "Mac", "-i", "2021-01-01")) shouldBe Some(
          Add("Mac", Some(LocalDate.parse("2021-01-01")), None)
        )
      }

      """it has an "add", a computer name, an introduced date and a discontinued date arguments""" in {
        ComputerCommand.parse(
          List("add", "-n", "Mac", "-i", "2021-01-01", "-d", "2021-12-31")
        ) shouldBe Some(
          Add("Mac", Some(LocalDate.parse("2021-01-01")), Some(LocalDate.parse(("2021-12-31"))))
        )
      }
    }

    "returns none" when {
      """it has an "add" argument only""" in {
        ComputerCommand.parse(List("add")) shouldBe None
      }

      """introduced date is invalid and no discontinued date""" in {
        ComputerCommand.parse(List("add", "-n", "Mac", "-i", "2021-13-01")) shouldBe None
      }

      """introduced date is valid and discontinued date is invalid""" in {
        ComputerCommand.parse(
          List("add", "-n", "Mac", "-i", "2021-01-01", "-d", "2021-13-31")
        ) shouldBe None
      }

    }
  }
}
