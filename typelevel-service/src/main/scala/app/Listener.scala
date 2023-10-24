package app

import app.Listener.ErrorReporter
import cats.effect._
import cats.effect.std.Console
import cats.implicits._
import domain.Streamable
import io.circe.Error

import scala.reflect.runtime.{universe => ru}

abstract class Listener[F[_]: Async: Concurrent: Console, CC <: Streamable: ru.TypeTag, ERR](implicit
    ep: ErrorReporter[F, ERR],
    source: Source[F, CC, ERR]
) {

  private def reportfulKafkaStream: fs2.Stream[F, CC] = source.listen
    .evalMapFilter {
      _.record.value match {
        case Left(err)     => ep.report(err).map(_ => none[CC])
        case Right(record) => record.some.pure[F]
      }

    }

  protected def executeEffects(initStream: fs2.Stream[F, CC]): fs2.Stream[F, Unit]

  final def listen: fs2.Stream[F, Unit] = executeEffects(reportfulKafkaStream)

}

object Listener {

  trait ErrorReporter[+F[_], -ERR] {
    def report(err: ERR): F[Unit]
  }

  implicit def cicreCatsEffectErrorReport[F[_]: Console]: ErrorReporter[F, Error] = (err: Error) =>
    Console[F].println(err.show)

}
