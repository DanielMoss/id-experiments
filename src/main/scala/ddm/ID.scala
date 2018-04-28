package ddm

import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, HCursor, Json}

import scala.reflect.runtime.universe.TypeTag

object ID {
  implicit def encoder[T : TypeEncoder]: Encoder[ID[T]] = {
    case mono: MonoID[T] => mono.asJson
    case dual: DualID[T] => dual.asJson
  }

  implicit def monoIDDecoder[T : HasMonoID
                               : HasMonoXorDualID
                               : Decoder
                               : TypeTag]: Decoder[ID[T]] =
    upcastDecoder[MonoID[T], T]

  implicit def dualIDDecoder[T : HasDualID
                               : HasMonoXorDualID
                               : Decoder
                               : TypeTag]: Decoder[ID[T]] =
    upcastDecoder[DualID[T], T]

  private def upcastDecoder[L <: ID[T] : Decoder, T]: Decoder[ID[T]] =
    Decoder[L].map(s => s: ID[T])

  val jsonTypeFieldName: String = "type"

  def apply[T : HasMonoID
              : HasMonoXorDualID
              : TypeTag](raw: String): ID[T] = MonoID(raw)

  def apply[T : HasDualID
              : HasMonoXorDualID
              : TypeTag](first: String, second: String): ID[T] = DualID(first, second)
}

sealed trait ID[+T] extends GenericMatching[ID[T]]

object MonoID {
  implicit def encoder[T : TypeEncoder]: Encoder[MonoID[T]] =
    (a: MonoID[T]) =>
      Json.obj(
        ID.jsonTypeFieldName -> TypeEncoder[T].asJson,
        jsonRawFieldName     -> a.raw.asJson
      )

  implicit def decoder[T : HasMonoID
                         : HasMonoXorDualID
                         : Decoder
                         : TypeTag]: Decoder[MonoID[T]] =
    (c: HCursor) =>
      for {
        _   <- c.downField(ID.jsonTypeFieldName).as[T]
        raw <- c.downField(jsonRawFieldName).as[String]
      } yield MonoID(raw)

  private val jsonRawFieldName: String = "raw"
}

final case class MonoID[T : HasMonoID
                          : HasMonoXorDualID
                          : TypeTag](raw: String) extends ID[T]

object DualID {
  implicit def encoder[T : TypeEncoder]: Encoder[DualID[T]] =
    (a: DualID[T]) =>
      Json.obj(
        ID.jsonTypeFieldName -> TypeEncoder[T].asJson,
        jsonFirstFieldName   -> a.first.asJson,
        jsonSecondFieldName  -> a.second.asJson
      )

  implicit def decoder[T : HasDualID
                         : HasMonoXorDualID
                         : Decoder
                         : TypeTag]: Decoder[DualID[T]] =
    (c: HCursor) =>
      for {
        _      <- c.downField(ID.jsonTypeFieldName).as[T]
        first  <- c.downField(jsonFirstFieldName).as[String]
        second <- c.downField(jsonSecondFieldName).as[String]
      } yield DualID(first, second)

  private val jsonFirstFieldName:  String = "first"
  private val jsonSecondFieldName: String = "second"
}

final case class DualID[T : HasDualID
                          : HasMonoXorDualID
                          : TypeTag](first: String, second: String) extends ID[T]

// Evidence that the type expects to have a mono ID
object HasMonoID {
  def apply[T]: HasMonoID[T] = new HasMonoID[T] {}
}
sealed trait HasMonoID[T]

// Evidence that the type expects to have dual IDs
object HasDualID {
  def apply[T]: HasDualID[T] = new HasDualID[T] {}
}
sealed trait HasDualID[T]

// Evidence that the type expects to have either a mono ID or dual IDs, but not both
object HasMonoXorDualID {
  implicit def hasMono[T : HasMonoID]: HasMonoXorDualID[T] = HasMonoXorDualID.apply
  implicit def hasDual[T : HasDualID]: HasMonoXorDualID[T] = HasMonoXorDualID.apply

  private def apply[T]: HasMonoXorDualID[T] = new HasMonoXorDualID[T] {}
}
sealed trait HasMonoXorDualID[T]
