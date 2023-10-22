package domain

case class Model(weights: IndexedSeq[Float], bias: Float) extends Streamable
