package app

import scala.math.exp

case class LogRegModel(weights: Vector[Float], bias: Float) {

  /**
   * scalar product of two vectors
   * matching length ensured by calling function
   *
   * @param left  - left operand of product
   * @param right - right operand of product
   * @return scalar product
   */
  private def scalarProduct(left: Vector[Float], right: Vector[Float]): Float = {
    left.zip(right).map { case (leftElement, rightElement) => leftElement * rightElement }.sum
  }

  private def normalizeValue(value: Float): Float = (1 / (1 + exp(-value))).toFloat

  override def toString: String = s"weights:${weights.mkString("[", ",", "]")}, bias: $bias"

  def getScore(sample: Vector[Float]): Float = {
    require(sample.length == weights.length, "Sample and weights size not match")

    val notNormalizedValue = bias + scalarProduct(sample, weights)

    normalizeValue(notNormalizedValue)
  }

}
