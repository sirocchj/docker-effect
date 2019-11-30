import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

final class IncorrectCommandsCheck extends AnyWordSpecLike with Matchers {
  "printing incorrect commands command" should {
    "not compile" in {
      """
        |import docker.effect.algebra.algebra._, shapeless.{::, HNil}
        |print0[docker :: kill :: HNil]
      """.stripMargin shouldNot compile

      """
        |import docker.effect.algebra.algebra._, shapeless.{::, HNil}
        |print0[docker :: rmi :: HNil]
      """.stripMargin shouldNot compile

      """
        |import docker.effect.algebra.algebra._, shapeless.{::, HNil}
        |print1[docker :: rmi :: Id :: HNil](fd484f19954f)
      """.stripMargin shouldNot compile

      """
        |import docker.effect.algebra.algebra._, shapeless.{::, HNil}
        |print1[docker :: rmi :: Repo :: HNil]("test-repo")
      """.stripMargin shouldNot compile

      """
        |import docker.effect.algebra.algebra._, shapeless.{::, HNil}
        |print0[kill :: signal :: KILL :: HNil]
      """.stripMargin shouldNot compile
    }
  }
}
