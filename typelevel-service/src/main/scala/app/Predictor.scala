package app

import app.listener.{ModelListener, PredictionListener, Source}
import cats.effect._
import cats.effect.std.Console
import cats.implicits._
import config.MLServiceConfig.AppConfig
import domain._
import fs2.kafka._
import io.circe._

object Predictor {

  def predict[F[_]: Async: Concurrent: Console: MathAlgebra](
      predictorConsumerSettings: ConsumerSettings[F, String, Either[Error, Prediction]],
      modelConsumerSettings: ConsumerSettings[F, String, Either[Error, Model]],
      appConfig: AppConfig
  ): F[Unit] = {
    implicit val ps = Source.kafkaSourceCirceDeser(predictorConsumerSettings)
    implicit val ms = Source.kafkaSourceCirceDeser(modelConsumerSettings)

    for {
      mRef <- Ref.of[F, Option[Model]](none)
      ml = new ModelListener[F](mRef)
      pl = new PredictionListener[F](mRef, appConfig)
      _ <- (ml.listen concurrently pl.listen).compile.drain
    } yield ()
  }

}
