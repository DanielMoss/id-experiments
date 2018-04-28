package ddm

import enumeratum.{Enum, EnumEntry}
import io.circe.generic.extras.semiauto._
import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, HCursor}

import scala.collection.immutable
import scala.reflect.ClassTag

sealed trait VehicleType extends EnumEntry

object VehicleType extends Enum[VehicleType] {
  implicit val encoder: Encoder[VehicleType] = deriveEnumerationEncoder
  implicit val decoder: Decoder[VehicleType] = deriveEnumerationDecoder

  implicit def subtypeEncoder[T <: VehicleType]: Encoder[T] = encoder.contramap(t => t: VehicleType)
  implicit def subtypeDecoder[T <: VehicleType : ClassTag]: Decoder[T] = decoder.emap {
    case t: T => Right(t)
    case s: VehicleType =>
      val className = implicitly[ClassTag[T]].runtimeClass.getSimpleName
      Left(s"Deserialised $s when expecting a $className")
  }

  implicit val idEncoder: Encoder[ID[VehicleType]] = {
    case t: ID[Car.type @unchecked]   if t.conformsTo[ID[Car.type]]   => t.asJson
    case t: ID[Van.type @unchecked]   if t.conformsTo[ID[Van.type]]   => t.asJson
    case t: ID[Lorry.type @unchecked] if t.conformsTo[ID[Lorry.type]] => t.asJson
    case t: ID[Wagon.type @unchecked] if t.conformsTo[ID[Wagon.type]] => t.asJson
  }

  implicit val idDecoder: Decoder[ID[VehicleType]] = (c: HCursor) =>
    c.downField(ID.jsonTypeFieldName).as[VehicleType].flatMap {
      case Car   => Decoder[ID[Car.type]].apply(c)
      case Van   => Decoder[ID[Van.type]].apply(c)
      case Lorry => Decoder[ID[Lorry.type]].apply(c)
      case Wagon => Decoder[ID[Wagon.type]].apply(c)
    }

  val values: immutable.IndexedSeq[VehicleType] = findValues

  case object Car   extends VehicleType { implicit val idType: HasMonoID[Car.type] = HasMonoID.apply }
  case object Van   extends VehicleType { implicit val idType: HasMonoID[Van.type] = HasMonoID.apply }
  case object Lorry extends VehicleType { implicit val idType: HasDualID[Lorry.type] = HasDualID.apply }
  case object Wagon extends VehicleType { implicit val idType: HasDualID[Wagon.type] = HasDualID.apply }
}
