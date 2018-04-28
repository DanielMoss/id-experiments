package ddm

import scala.reflect.runtime.universe.{TypeTag, typeOf}

abstract class GenericMatching[+T : TypeTag] {
  def conformsTo[S : TypeTag]: Boolean =
    typeOf[T] <:< typeOf[S]
}
