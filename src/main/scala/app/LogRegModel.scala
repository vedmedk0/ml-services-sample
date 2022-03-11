package app

import scala.math.exp

case class LogRegModel(weights: Vector[Float], bias: Float) {

  /**
   * Scalar product of two vectors.
   * Matching length ensured by calling function.
   * CHALLENGE NOTE: math libraries can be used here instead of collection transformations
   * @param left  - left operand of product
   * @param right - right operand of product
   * @return scalar product
   */
  private def scalarProduct(left: Vector[Float], right: Vector[Float]): Float = {
    left.zip(right).map { case (leftElement, rightElement) => leftElement * rightElement }.sum
  }

  /**
   * Fit value in (0, 1) range
   * @param value
   * @return
   */
  private def normalizeValue(value: Float): Float = (1 / (1 + exp(-value))).toFloat

  override def toString: String = s"weights:${weights.mkString("[", ",", "]")}, bias: $bias"

  /**
   * Get score of logistic regression model.
   * if weights are w1, w2, w3,... and bias is b, and sample is vector with  components x1, x2, x3, ...
   * then z = b + x1 * w1 + x2 * w2 + ... + xn * wn
   * score = 1 / (1 + exp(-z))
   * @param sample - vector of sample values
   * @return score, float ranged from 0 to 1
   */
  def getScore(sample: Vector[Float]): Float = {
    require(sample.length == weights.length, "Sample and weights size not match")

    val notNormalizedValue = bias + scalarProduct(sample, weights)

    normalizeValue(notNormalizedValue)
  }

}
