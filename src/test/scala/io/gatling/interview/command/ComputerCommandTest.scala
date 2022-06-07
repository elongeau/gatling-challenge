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

  """parsing a "list" command""" should {
    "returns a List command" when {
      """it has a single "list" arguments""" in {
        ComputerCommand.parse(List("list")) shouldBe Some(ListComputers)
      }

      """it has a "list" arguments and other arguments that are ignored""" in {
        ComputerCommand.parse(List("list", "foo")) shouldBe Some(ListComputers)
      }
    }
  }

  """parsing an "add" command""" should {
    "returns an Add Command" when {
      """it has an "add" and a computer name arguments""" in {
        ComputerCommand.parse(List("add", "-n", "Mac")) shouldBe Some(Add("Mac", None, None))
      }

      """it has an "add", a computer name and an introduced date arguments""" in {
        ComputerCommand.parse(List("add", "-n", "Mac", "-i", "2021-01-01")) shouldBe Some(
          Add("Mac", Some(LocalDate.parse("2021-01-01")), None)
        )
      }

      """it has an "add", a computer name and a discontinued date arguments""" in {
        ComputerCommand.parse(
          List("add", "-n", "Mac", "-d", "2021-12-31")
        ) shouldBe Some(
          Add("Mac", None, Some(LocalDate.parse(("2021-12-31"))))
        )
      }

      """it has an "add", a computer name, an introduced date and a discontinued date arguments""" in {
        ComputerCommand.parse(
          List("add", "-n", "Mac", "-i", "2021-01-01", "-d", "2021-12-31")
        ) shouldBe Some(
          Add("Mac", Some(LocalDate.parse("2021-01-01")), Some(LocalDate.parse(("2021-12-31"))))
        )
      }

      """it has an "add", a computer name, an introduced date, a discontinued date arguments in any order""" in {
        ComputerCommand.parse(
          List("add", "-d", "2021-12-31", "-i", "2021-01-01", "-n", "Mac")
        ) shouldBe Some(
          Add("Mac", Some(LocalDate.parse("2021-01-01")), Some(LocalDate.parse(("2021-12-31"))))
        )
      }

      """introduced date is invalid""" in {
        ComputerCommand.parse(List("add", "-n", "Mac", "-i", "2021-13-01")) shouldBe Some(
          Add("Mac", None, None)
        )
      }

      """discontinued date is invalid""" in {
        ComputerCommand.parse(List("add", "-n", "Mac", "-d", "2021-13-31")) shouldBe Some(
          Add("Mac", None, None)
        )
      }
    }

    "returns none" when {
      """it has an "add" argument only""" in {
        ComputerCommand.parse(List("add")) shouldBe None
      }

      """it has no "name" argument""" in {
        ComputerCommand.parse(
          List("add", "-i", "2021-01-01", "-d", "2021-12-31")
        ) shouldBe None
      }

      """it has other argument than name, introduced and discontinued""" in {
        ComputerCommand.parse(
          List("add", "-n", "Mac", "-i", "2021-01-01", "-d", "2021-12-31", "-t")
        ) shouldBe None
      }

      """it has an odd number of argument""" in {
        ComputerCommand.parse(
          List("add", "-n", "Mac", "-i", "2021-01-01", "-d")
        ) shouldBe None
      }

    }
  }

  """parsing a "show" command""" should {
    "returns a Show command" when {
      """it has a "show" argument with a computer ID""" in {
        ComputerCommand.parse(List("show", "1")) shouldBe Some(Show(1))
      }
    }

    "returns no command" when {
      """it has a "show" only""" in {
        ComputerCommand.parse(List("show")) shouldBe None
      }

      """it has a "show" argument with a non numeric ID""" in {
        ComputerCommand.parse(List("show", "a")) shouldBe None
      }
    }
  }
}
