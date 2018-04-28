package ddm

import io.circe.syntax.EncoderOps
import io.circe.{Encoder, Json}

object TypeEncoder {
  def apply[T : TypeEncoder]: TypeEncoder[T] = implicitly[TypeEncoder[T]]

  implicit def encoder[T : ValueOf : Encoder]: TypeEncoder[T] =
    new TypeEncoder[T] { val asJson: Json = valueOf[T].asJson }
}

trait TypeEncoder[T] {
  def asJson: Json
}
