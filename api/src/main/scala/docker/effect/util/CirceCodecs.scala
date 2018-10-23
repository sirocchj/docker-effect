package docker
package effect
package util

import cats.Show
import cats.syntax.either._
import docker.effect.types.|
import io.circe._

object CirceCodecs {

  /**
    * Gives a Circe Encoder for the type `A` when a `Show` instance
    * is available for it.
    *
    * Example, Instant:
    *
    * val instantEncoder: Encoder[Instant] = encoderFor[Instant]
    *
    *
    * Example, Shapeless tag:
    *
    * def taggedLongEncoder[T]: Encoder[Long @@ T] = encoderFor[Long @@ T]
    *
    * def taggedStringEncoder[T]: Encoder[String @@ T] = encoderFor[String @@ T]
    *
    * def taggedBigDecimalEncoder[T]: Encoder[BigDecimal @@ T] = encoderFor[BigDecimal @@ T]
    *
    * @return An encoder for `A`
    */
  def stringEncoderFor[A](implicit ev: Show[A]): Encoder[A] =
    Encoder.encodeString.contramap[A](ev.show)

  /**
    * Gives a Circe Decoder for the type `A` when a way to go from String to `A`
    * is provided
    *
    * Example, Instant:
    *
    * val instantDecoder: Decoder[Instant] = decoderFor(Instant.parse)
    *
    * @return A decoder for `A`
    */
  def stringDecoderFor[A]: (String => A) => Decoder[A] =
    f => stringMappedDecoderFor(f, identity[A])

  /**
    * Gives a Circe Decoder for `A` that maps the successful decoded value with `f`
    *
    * Example, Shapeless tag:
    *
    * def taggedLongDecoder[T]: Decoder[Long @@ T] =
    *   mappedDecoderFor(_.toLong, tag[T].apply)
    *
    * def taggedBigDecimalDecoder[T]: Decoder[BigDecimal @@ T] =
    *   mappedDecoderFor(BigDecimal.apply, tag[T].apply)
    *
    * def taggedStringDecoder[T]: Decoder[String @@ T] =
    *   mappedDecoderFor(identity, tag[T].apply)
    *
    * @return A decoder for `A` that maps the result to `B` in case of successful decoding
    */
  def stringMappedDecoderFor[A, B](ff: String => A, f: A => B): Decoder[B] =
    stringMappedDecoderWithError(s => ff(s).asRight, f, s => s"Cannot parse $s")

  /**
    * Similar to `mappedDecoderFor` but the possibility of choosing a custom error message
    *
    * @return A decoder for `A` that maps the result to `B` in case of successful decoding and
    *         gives the provided error message in case of failure decoding
    */
  def stringMappedDecoderWithError[A, B](
    ff: String => String | A,
    f: A => B,
    err: String => String
  ): Decoder[B] =
    Decoder.decodeString emap { str =>
      ff(str) leftMap (_ => err(str)) map f
    }
}
