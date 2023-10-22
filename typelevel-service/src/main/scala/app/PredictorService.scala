package app

import app.PredictorService.AccuracyCounter
import cats.data._
import cats.effect._
import cats.effect.std.Console
import cats.implicits._
import config.MLServiceConfig.AppConfig
import domain._
import fs2.kafka._
import io.circe._
import kafka.KafkaHelpers

import scala.concurrent.duration._

class PredictorService[F[_]: Async: Concurrent: Console](
    predictorConsumerSettings: ConsumerSettings[F, String, Either[Error, Prediction]],
    modelConsumerSettings: ConsumerSettings[F, String, Either[Error, Model]],
    appConfig: AppConfig
) {
  //TODO: proper error handling & slf4j logging

  private val threshold: Float = appConfig.modelThreshold

  private val model: F[Ref[F, Option[Model]]] = Ref.of[F, Option[Model]](none)

  private val counter: F[Ref[F, AccuracyCounter]] = Ref.of[F, AccuracyCounter](AccuracyCounter())

  private def predictIfModelSet(prediction: Prediction, modelRef: Ref[F, Option[Model]]): F[Option[Boolean]] = (for {
    m <- OptionT(modelRef.get)
    result = MathOps.guessed(m, prediction, threshold)
  } yield result).orElseF(Console[F].println("No model set!").map(_ => none[Boolean])).value

  private def modifyCounter(guess: Boolean, counterRef: Ref[F, AccuracyCounter]): F[AccuracyCounter] =
    counterRef.updateAndGet(_.inc(guess))

  private def predictStream(
      modelRef: Ref[F, Option[Model]],
      counterRef: Ref[F, AccuracyCounter]
  ): fs2.Stream[F, Unit] = KafkaHelpers
    .reportfulKafkaStream[F, Prediction](predictorConsumerSettings)
    .evalMapFilter {
      predictIfModelSet(_, modelRef)
    }
    .evalMap {
      modifyCounter(_, counterRef)
    }
    .drop(appConfig.logPeriod - 1) // reports every `logPeriod` records
    .take(1)
    .repeat
    .evalTap(acc => Console[F].println(s"result:$acc, accuracy:${acc.accuracy()} "))
    .void

  private def listenModelsStream(modelRef: Ref[F, Option[Model]]) =
    KafkaHelpers
      .reportfulKafkaStream[F, Model](modelConsumerSettings)
      .evalTap { newModel =>
        modelRef.getAndSet(newModel.some).flatMap {
          case Some(oldModel) => Console[F].println(s"Changed model $oldModel to $newModel")
          case None           => Console[F].println(s"Set first model $newModel")
        }
      }
      .delayBy(5.second)

  def runService(): F[Unit] = for {
    mRef <- model
    cRef <- counter
    _    <- (listenModelsStream(mRef) concurrently predictStream(mRef, cRef)).compile.drain
  } yield ()

}

object PredictorService {
  private case class AccuracyCounter(guessed: Long = 0, processed: Long = 0) {
    def accuracy(): Float = if (processed == 0) 0 else guessed.toFloat / processed

    def inc(guessed: Boolean): AccuracyCounter =
      if (guessed) this.copy(this.guessed + 1, this.processed + 1) else this.copy(processed = this.processed + 1)
  }
}
