package generator.emitter

import cats._
import cats.effect._
import cats.effect.std.UUIDGen
import domain.Streamable
abstract class Emitter[F[_]: Async: Functor: UUIDGen, A <: Streamable](implicit
    sink: Sink[F, A]
) {

  protected def generate: fs2.Stream[F, A]

  final def emit: fs2.Stream[F, Unit] = sink.produce(generate)
}

object Emitter {
  def emit[F[_]: Async: Temporal](emitters: Emitter[F, _]*): F[Unit] =
    emitters.map(_.emit).reduce(_ concurrently _).compile.drain
}
