package emitters

import cats._
import cats.effect._
import cats.effect.std.UUIDGen
import domain.Streamable
abstract class Emitter[F[_]: Async: Functor: UUIDGen, A <: Streamable, B](implicit
    sink: Sink[F, A, B]
) {

  protected def generate: fs2.Stream[F, A]

  final def emit: fs2.Stream[F, B] = sink.produce(generate)
}

object Emitter {
  def emit[F[_]: Async: Temporal](emitters: Emitter[F, _, _]*): F[Unit] =
    emitters.map(_.emit).reduce(_ concurrently _).compile.drain
}
