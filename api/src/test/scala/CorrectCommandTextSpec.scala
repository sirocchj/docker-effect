import docker.effect.algebra.algebra._
import org.scalatest.{ Matchers, WordSpecLike }
import shapeless.{ ::, HNil }

final class CorrectCommandTextSpec extends WordSpecLike with Matchers {

  "printing command" should {
    "produce the expected text" in {
      print0[docker :: images :: HNil]                         shouldBe "docker images"
      print0[docker :: images :: all :: HNil]                  shouldBe "docker images --all"
      print0[docker :: images :: q :: HNil]                    shouldBe "docker images -q"
      print0[docker :: kill :: signal :: KILL :: HNil]         shouldBe "docker kill --signal=KILL"
      print0[docker :: kill :: s :: HUP :: HNil]               shouldBe "docker kill -s=HUP"
      print1[docker :: rmi :: Id :: HNil](Id("fd484f19954f"))  shouldBe "docker rmi fd484f19954f"
      print1[docker :: rmi :: Repo :: HNil](Repo("test-repo")) shouldBe "docker rmi test-repo"
    }
  }
}
