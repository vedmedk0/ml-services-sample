package domain

import scala.reflect.runtime.{universe => ru}
trait Streamable {

  def getTopicName: String = this.getClass.getSimpleName.toLowerCase

}

object Streamable {
  def getTopicName[A <: Streamable: ru.TypeTag](): String =
    ru.typeOf[A].typeSymbol.name.toString.toLowerCase

}
