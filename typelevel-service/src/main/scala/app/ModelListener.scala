package app

import cats.effect._
import cats.effect.std.Console
import cats.implicits._
import domain.Model
import fs2.kafka.ConsumerSettings
import io.circe.Error

import scala.concurrent.duration.DurationInt

class ModelListener[F[_]: Async: Concurrent: Console](
    modelRef: Ref[F, Option[Model]]
)(implicit source: Source[F, Model, Error])
    extends Listener[F, Model, Error] {
  //TODO: proper error handling & slf4j logging

  override protected def executeEffects(initStream: fs2.Stream[F, Model]): fs2.Stream[F, Unit] =
    initStream
      .evalMap { newModel =>
        modelRef.getAndSet(newModel.some).flatMap {
          case Some(oldModel) => Console[F].println(s"Changed model $oldModel to $newModel")
          case None           => Console[F].println(s"Set first model $newModel")
        }
      }
      .delayBy(3.second)

}
