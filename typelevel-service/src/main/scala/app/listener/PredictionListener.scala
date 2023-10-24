package app.listener

import app.listener.PredictionListener.AccuracyCounter
import cats.data.OptionT
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
  //TODO: proper error handling & slf4j logging

  private def guessed(model: Model, prediction: Prediction): OptionT[F, Boolean] =
    MathAlgebra[F]
      .getScore(prediction.vector, model.weights, model.bias)
      .map(score => abs(score - prediction.label) < appConfig.modelThreshold)

  private def predictIfModelSet(prediction: Prediction, modelRef: Ref[F, Option[Model]]): F[Option[Boolean]] =
    (for {
      m      <- OptionT(modelRef.get).toRight("No model set!")
      result <- guessed(m, prediction).toRight("Dimensions not equal")
    } yield result)
      .foldF(
        errorMsg => Console[F].println(errorMsg).map(_ => none),
        _.some.pure[F]
      )

  private def modifyCounter(guess: Boolean, counterRef: Ref[F, AccuracyCounter]): F[AccuracyCounter] =
    counterRef.updateAndGet(_.inc(guess))

  override protected def executeEffects(initStream: fs2.Stream[F, Prediction]): fs2.Stream[F, Unit] =
    fs2.Stream.eval(Ref.of[F, AccuracyCounter](AccuracyCounter())).flatMap { cRef =>
      initStream
        .evalMapFilter(predictIfModelSet(_, modelRef))
        .evalMap(modifyCounter(_, cRef))
        .drop(appConfig.logPeriod - 1) // reports every `logPeriod` records
        .take(1)
        .repeat
        .evalTap(acc => Console[F].println(s"result:$acc, accuracy:${acc.accuracy()} "))
        .void
    }

}

object PredictionListener {
  private case class AccuracyCounter(guessed: Long = 0, processed: Long = 0) {
    def accuracy(): Float = if (processed == 0) 0 else guessed.toFloat / processed

    def inc(guessed: Boolean): AccuracyCounter =
      if (guessed) this.copy(this.guessed + 1, this.processed + 1) else this.copy(processed = this.processed + 1)
  }
}
