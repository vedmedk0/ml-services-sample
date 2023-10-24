package app

import app.listener.{ModelListener, PredictionListener, Source}
import cats.effect._
import cats.effect.std.Console
import cats.implicits._
import config.MLServiceConfig.AppConfig
import domain._
import io.circe._

object Predictor {

  def predict[F[_]: Async: Concurrent: Console: MathAlgebra](
      appConfig: AppConfig
  )(implicit modelSource: Source[F, Model, Error], predictionSource: Source[F, Prediction, Error]): F[Unit] =
    for {
      mRef <- Ref.of[F, Option[Model]](none)
      ml = new ModelListener[F](mRef)
      pl = new PredictionListener[F](mRef, appConfig)
      _ <- (ml.listen concurrently pl.listen).compile.drain
    } yield ()

}
