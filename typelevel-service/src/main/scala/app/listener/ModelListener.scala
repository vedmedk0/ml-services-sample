package app.listener

import app.listener.ListenerResults._
import cats.conversions.all._
import cats.effect._
import cats.effect.std.Console
import cats.implicits._
import domain.Model
import io.circe.Error

import scala.concurrent.duration.DurationInt

class ModelListener[F[_]: Async: Concurrent: Console](
    modelRef: Ref[F, Option[Model]]
)(implicit source: Source[F, Model, Error])
    extends Listener[F, Model, Error] {

  override protected def streamLogic(
      initStream: fs2.Stream[F, Either[Error, Model]]
  ): fs2.Stream[F, PredictorResult] =
    initStream
      .evalMap { modelMaybe =>
        modelMaybe.fold[F[PredictorResult]](
          ParseError(_).pure[F],
          { newModel =>
            modelRef
              .getAndSet(newModel.some)
              .map(_.fold[PredictorResult](InitModel(newModel))(ChangedModel(_, newModel)))
          }
        )
      }
      .delayBy(3.second)
}
