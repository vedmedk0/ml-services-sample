package domain

case class Prediction(vector: IndexedSeq[Float], label: Float) extends Streamable
