package app

import app.listener.ListenerResults._
import app.listener.{ModelListener, PredictionListener, Source}
import cats.effect._
import cats.effect.std.Console
import cats.implicits._
import config.MLServiceConfig.AppConfig
import domain._
import io.circe._
import listener.ListenerResults._

object Predictor {

  def predict[F[_]: Async: Concurrent: Console: MathAlgebra](
      appConfig: AppConfig
  )(implicit modelSource: Source[F, Model, Error], predictionSource: Source[F, Prediction, Error]): F[Unit] = {

    def handleResults(stream: fs2.Stream[F, PredictorResult]): fs2.Stream[F, Unit] =
      stream
        .handleError(e => OtherError(e.getMessage)) // unexpected exception halts both streams
        .filter {
          case _: PredictorError                                                          => true
          case success: PredictorSuccess if success.reportDensity == ReportDensity.Sparse => true
          case report: Report if report.ac.processed % appConfig.logPeriod == 0 => true
          case _ => false
        }
        .printlns
        .void

    for {
      mRef <- Ref.of[F, Option[Model]](none)
      ml     = new ModelListener[F](mRef)
      pl     = new PredictionListener[F](mRef, appConfig)
      merged = ml.listen merge pl.listen
      _ <- merged.through(handleResults).compile.drain
    } yield ()

  }

}
