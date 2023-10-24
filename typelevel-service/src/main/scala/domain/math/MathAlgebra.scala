package domain.math

import cats._
import cats.data._
import cats.effect._
import cats.implicits._

import scala.math.exp

trait MathAlgebra[F[_]] {

  def scalarProduct(left: IndexedSeq[Float], right: IndexedSeq[Float]): F[Float]

  def normalizeValue(value: Float): F[Float]

  def getScore(vector: IndexedSeq[Float], weights: IndexedSeq[Float], bias: Float): OptionT[F, Float]

}

object MathAlgebra {

  def apply[G[_]](implicit A: MathAlgebra[G]): MathAlgebra[G] = implicitly[MathAlgebra[G]]

  implicit def ceAlgebra[G[_]: Sync]: MathAlgebra[G] = new MathAlgebra[G] {
    override def scalarProduct(left: IndexedSeq[Float], right: IndexedSeq[Float]): G[Float] =
      Sync[G].delay(left.zip(right).map { case (leftElement, rightElement) => leftElement * rightElement }.sum)

    override def normalizeValue(value: Float): G[Float] = Sync[G].delay((1 / (1 + exp(-value))).toFloat)

    override def getScore(vector: IndexedSeq[Float], weights: IndexedSeq[Float], bias: Float): OptionT[G, Float] = {
      OptionT(FlatMap[G].flatMap(Sync[G].delay(vector.length == weights.length)) {
        case false => none[Float].pure[G]
        case true =>
          for {
            sp         <- scalarProduct(vector, weights)
            normalized <- normalizeValue(sp + bias)
          } yield normalized.some

      })
    }
  }

}
