package ddm

import ddm.VehicleType.{Lorry, Car, Wagon, Van}
import io.circe.Decoder
import io.circe.parser.decode
import io.circe.syntax._
import org.scalatest.FreeSpec
import org.scalatest.Matchers._

import scala.collection.immutable
import scala.reflect.ClassTag

final class VehicleTypeSpec extends FreeSpec {
  private val allTypes: immutable.IndexedSeq[VehicleType] = VehicleType.values

  "Serialisation" - {
    def expectedFormat(t: VehicleType): String = s""""${t.entryName}""""

    "Encoding" - {
      def testEncodingAs[T <: VehicleType](t: T): Unit =
        s"$t" in (t.asJson.noSpaces shouldBe expectedFormat(t))

      "Should encode correctly" -
        allTypes.foreach {
          case t @ Car   => testEncodingAs[Car.type](t)
          case t @ Van   => testEncodingAs[Van.type](t)
          case t @ Lorry => testEncodingAs[Lorry.type](t)
          case t @ Wagon => testEncodingAs[Wagon.type](t)
        }

      s"Should encode correctly when cast as a $VehicleType" -
        allTypes.foreach(testEncodingAs[VehicleType])
    }

    "Decoding" - {
      def testDecodingEachTypeAs[T <: VehicleType : ClassTag : Decoder](): Unit =
        s"Decoding as ${implicitly[ClassTag[T]].runtimeClass.getSimpleName}" -
          allTypes.foreach {
            case t: T =>
              s"$t should succeed" in (decode[T](expectedFormat(t)) shouldBe Right(t))
            case other: VehicleType =>
              s"$other should fail" in (decode[T](expectedFormat(other)) shouldBe a [Left[_, _]])
          }

      testDecodingEachTypeAs[VehicleType]()
      allTypes.foreach {
        case Car   => testDecodingEachTypeAs[Car.type]()
        case Van   => testDecodingEachTypeAs[Van.type]()
        case Lorry => testDecodingEachTypeAs[Lorry.type]()
        case Wagon => testDecodingEachTypeAs[Wagon.type]()
      }
    }
  }
}
