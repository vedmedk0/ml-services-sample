package app.listener

import app.listener.ListenerResults._
import app.listener.PredictionListener.AccuracyCounter
import cats.data._
import cats.effect._
import cats.effect.std.Console
import cats.implicits._
import config.MLServiceConfig.AppConfig
import domain.{MathAlgebra, Model, Prediction}
import io.circe.Error

import scala.math.abs

class PredictionListener[F[_]: Async: Concurrent: Console: MathAlgebra](
    modelRef: Ref[F, Option[Model]],
    appConfig: AppConfig
)(implicit source: Source[F, Prediction, Error])
    extends Listener[F, Prediction, Error] {

  private def guessed(model: Model, prediction: Prediction): OptionT[F, Boolean] =
    MathAlgebra[F]
      .getScore(prediction.vector, model.weights, model.bias)
      .map(score => abs(score - prediction.label) < appConfig.modelThreshold)

  private def predictIfModelSet(
      predictionMaybe: Either[Error, Prediction],
      modelRef: Ref[F, Option[Model]],
      counterRef: Ref[F, AccuracyCounter]
  ): F[PredictorResult] =
    (for {
      prediction <- EitherT.fromEither[F](predictionMaybe.leftMap(ParseError))
      model      <- OptionT(modelRef.get).toRight(NoModelError())
      guess      <- guessed(model, prediction).toRight[PredictorError](DimensionsNotEqualError(prediction, model))
      ac <- OptionT
        .liftF(modifyCounter(guess, counterRef))
        .toRight[PredictorError](OtherError("Impossible to make error here"))
    } yield Report(ac)).merge

  private def modifyCounter(guess: Boolean, counterRef: Ref[F, AccuracyCounter]): F[AccuracyCounter] =
    counterRef.updateAndGet(_.inc(guess))

  override protected def streamLogic(
      initStream: fs2.Stream[F, Either[Error, Prediction]]
  ): fs2.Stream[F, PredictorResult] =
    fs2.Stream.eval(Ref.of[F, AccuracyCounter](AccuracyCounter())).flatMap { cRef =>
      initStream
        .evalMap(a => predictIfModelSet(a, modelRef, cRef))
    }

}

object PredictionListener {
  private[listener] case class AccuracyCounter(guessed: Long = 0, processed: Long = 0) {
    def accuracy: Float = if (processed == 0) 0 else guessed.toFloat / processed

    def inc(guessed: Boolean): AccuracyCounter =
      if (guessed) this.copy(this.guessed + 1, this.processed + 1) else this.copy(processed = this.processed + 1)
  }
}
