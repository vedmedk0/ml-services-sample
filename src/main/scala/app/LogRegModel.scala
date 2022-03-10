package app

import scala.math.exp

case class LogRegModel(weigts: Array[Float], bias: Float) {


  /**
   * scalar product of two vectors
   * matching length ensured by calling function
   *
   * @param left  - left operand of product
   * @param right - right operand of product
   * @return scalar product
   */
  private def scalarProduct(left: Array[Float], right: Array[Float]): Float = {
    left.zip(right).map { case (leftElement, rightElement) => leftElement * rightElement }.sum
  }

  private def normalizeValue(value: Float): Float = (1 / (1 + exp(-value))).toFloat

  def getScore(sample: Array[Float]): Float = {
    require(sample.length == weigts.length, "sample and weights size not match")

    val notNormalizedValue = bias + scalarProduct(sample, weigts)

    normalizeValue(notNormalizedValue)
  }

}
