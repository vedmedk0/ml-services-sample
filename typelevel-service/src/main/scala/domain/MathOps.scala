package domain

import scala.math.{abs, exp}

object MathOps {

  private def scalarProduct(left: IndexedSeq[Float], right: IndexedSeq[Float]): Float = {
    left.zip(right).map { case (leftElement, rightElement) => leftElement * rightElement }.sum
  }
  private def normalizeValue(value: Float): Float = (1 / (1 + exp(-value))).toFloat

  private def getScore(vector: IndexedSeq[Float], weights: IndexedSeq[Float], bias: Float): Float = {
    //TODO: Proper error handling
    require(vector.length == weights.length, "Sample and weights size not match")

    val notNormalizedValue = bias + scalarProduct(vector, weights)

    normalizeValue(notNormalizedValue)

  }

  def guessed(model: Model, prediction: Prediction, threshold: Float): Boolean =
    abs(getScore(prediction.vector, model.weights, model.bias) - prediction.label) < threshold

}
