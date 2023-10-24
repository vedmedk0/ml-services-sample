package generator.emitter

import cats.Functor
import cats.effect.Async
import cats.effect.std.{Console, UUIDGen}
import cats.implicits._
import domain.Streamable
import fs2.kafka._

trait Sink[F[_], -A] {

  def produce(stream: fs2.Stream[F, A]): fs2.Stream[F, Unit]

}

object Sink {
  implicit def kafkaSink[F[_]: Async: UUIDGen: Functor, A <: Streamable](
      producerSettings: ProducerSettings[F, String, A]
  ): Sink[F, A] =
    new Sink[F, A] {
      private def objectToRecord(obj: A): F[ProducerRecords[String, A]] =
        Functor[F].map(UUIDGen[F].randomUUID) { uuid =>
          ProducerRecords.one(ProducerRecord(obj.getTopicName, uuid.toString, obj))
        }

      override def produce(stream: fs2.Stream[F, A]): fs2.Stream[F, Unit] = stream
        .evalMap {
          objectToRecord
        }
        .through(KafkaProducer.pipe(producerSettings)).void
    }

  implicit def consoleSink[F[_]: Async: Functor: Console, A]: Sink[F, A] = (stream: fs2.Stream[F, A]) =>
    stream.observe(_.printlns).void

}
