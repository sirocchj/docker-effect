package instances

import cats.effect.IO
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers
import zio.Task

sealed trait TestRun[F[_]] extends Matchers {
  def successAssert[A](fa: F[A])(assert: A => Assertion): Assertion
  def failureAssert[A](fa: F[A])(assert: Throwable => Assertion): Assertion
}

object TestRun {
  @inline final def apply[F[_]](implicit ev: TestRun[F]): TestRun[F] = implicitly

  implicit def zioTestRun: TestRun[Task] =
    new TestRun[Task] {
      def successAssert[A](fa: Task[A])(assert: A => Assertion): Assertion =
        zio.Runtime.default
          .unsafeRun(fa.either)
          .fold(err => fail(s"Expected success but got $err"), assert)

      def failureAssert[A](fa: Task[A])(assert: Throwable => Assertion): Assertion =
        zio.Runtime.default
          .unsafeRun(fa.either)
          .fold(assert, res => fail(s"Expected failure but got $res"))
    }

  implicit def catsTestRun: TestRun[IO] =
    new TestRun[IO] {
      def successAssert[A](fa: IO[A])(assert: A => Assertion): Assertion =
        fa.redeem(err => fail(s"Expected success but got $err"), assert).unsafeRunSync

      def failureAssert[A](fa: IO[A])(assert: Throwable => Assertion): Assertion =
        fa.redeem(assert, res => fail(s"Expected failure but got $res")).unsafeRunSync
    }
}
