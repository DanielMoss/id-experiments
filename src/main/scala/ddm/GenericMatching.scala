package ddm

import scala.reflect.runtime.universe.{TypeTag, typeOf}

abstract class GenericMatching[+T : TypeTag] {
  final def conformsTo[S : TypeTag]: Boolean =
    typeOf[T] <:< typeOf[S]
}
