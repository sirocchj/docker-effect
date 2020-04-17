import _root_.docker.effect.Container
import _root_.docker.effect.algebra.{ Name, _ }
import _root_.docker.effect.interop.Provider
import _root_.docker.effect.syntax.provider._
import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.show._
import instances.TestRun
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import syntax.TestSyntax
import zio.interop.catz._

sealed abstract class ExecutionCheck[F[-_, _], G[_]](container: Container[F, G], name: String)(
  implicit
  ev2: Provider[F, G],
  ev4: TestRun[G],
  syn: Sync[G]
) extends AnyWordSpecLike
    with Matchers
    with TestSyntax {

  import container.docker._

  s"a $name docker effect" should {
    "get the list of images" in {
      listAllImages.providedUnit satisfies { res =>
        val resText = res.show
        resText should startWith("REPOSITORY")
        resText should include("TAG")
        resText should include("IMAGE ID")
        resText should include("CREATED")
        resText should include("SIZE")
      }
    }

    "start a redis instance" in {
      TestRun.unsafe(
        container.detached(Name("redis"), latest).use { id =>
          listAllContainerIds.providedUnit.map { sm =>
            sm.unMk should include(id.value)
          } >> syn.unit
        }
      )
    }

    "start a redis instance mapping the port" in {
      TestRun.unsafe(
        container.detached(Name("redis")).use { id =>
          listAllContainerIds.providedUnit.map { sm =>
            sm.unMk should include(id.value)
          } >> syn.unit
        }
      )
    }
  }
}

final class ZioExecutionCheck  extends ExecutionCheck(Container.zio, "Zio")
final class CatsExecutionCheck extends ExecutionCheck(Container.catsIo, "Cats IO")
