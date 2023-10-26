package app.listener

import app.listener.ListenerResults.PredictorResult
import cats.effect._
import cats.effect.std.Console
import domain.Streamable

import scala.reflect.runtime.{universe => ru}

abstract class Listener[F[_]: Async: Concurrent: Console, CC <: Streamable: ru.TypeTag, ERR](implicit
    source: Source[F, CC, ERR]
) {

  protected def streamLogic(
      initStream: fs2.Stream[F, Either[ERR, CC]]
  ): fs2.Stream[F, PredictorResult]

  final def listen: fs2.Stream[F, PredictorResult] =
    source.listen.map(_.record.value).through(streamLogic)

}
