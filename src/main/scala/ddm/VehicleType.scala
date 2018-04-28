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

  implicit val idEncoder: Encoder[ID[VehicleType]] =
    (a: ID[VehicleType]) => a.matching(
      onCar   = _.asJson,
      onVan   = _.asJson,
      onLorry = _.asJson,
      onWagon = _.asJson
    )

  implicit val idDecoder: Decoder[ID[VehicleType]] = (c: HCursor) =>
    c.downField(ID.jsonTypeFieldName).as[VehicleType].flatMap {
      case Car   => Decoder[ID[Car.type]].apply(c)
      case Van   => Decoder[ID[Van.type]].apply(c)
      case Lorry => Decoder[ID[Lorry.type]].apply(c)
      case Wagon => Decoder[ID[Wagon.type]].apply(c)
    }

  implicit class IDOps(self: ID[VehicleType]) {
    def matching[T](onCar: MonoID[Car.type] => T,
                    onVan: MonoID[Van.type] => T,
                    onLorry: DualID[Lorry.type] => T,
                    onWagon: DualID[Wagon.type] => T): T =
      self match {
        case t: MonoID[Car.type @unchecked]   if t.conformsTo[ID[Car.type]]   => onCar(t)
        case t: MonoID[Van.type @unchecked]   if t.conformsTo[ID[Van.type]]   => onVan(t)
        case t: DualID[Lorry.type @unchecked] if t.conformsTo[ID[Lorry.type]] => onLorry(t)
        case t: DualID[Wagon.type @unchecked] if t.conformsTo[ID[Wagon.type]] => onWagon(t)
      }
  }

  val values: immutable.IndexedSeq[VehicleType] = findValues

  case object Car   extends VehicleType { implicit val idType: HasMonoID[Car.type] = HasMonoID.apply }
  case object Van   extends VehicleType { implicit val idType: HasMonoID[Van.type] = HasMonoID.apply }
  case object Lorry extends VehicleType { implicit val idType: HasDualID[Lorry.type] = HasDualID.apply }
  case object Wagon extends VehicleType { implicit val idType: HasDualID[Wagon.type] = HasDualID.apply }
}
