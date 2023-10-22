package kafka

import cats.Functor
import cats.effect.std.{Console, UUIDGen}
import cats.effect.{Async, Concurrent}
import cats.implicits._
import domain.Streamable
import fs2.kafka.{ConsumerSettings, KafkaConsumer, ProducerRecord, ProducerRecords}
import io.circe.Error

import scala.reflect.runtime.{universe => ru}

object KafkaHelpers {
  def objectToRecord[G[_]: UUIDGen: Functor, A <: Streamable](obj: A): G[ProducerRecords[String, A]] =
    Functor[G].map(UUIDGen[G].randomUUID) { uuid =>
      ProducerRecords.one(ProducerRecord(obj.getTopicName, uuid.toString, obj))
    }

  def reportfulKafkaStream[F[_]: Async: Concurrent: Console, CC <: Streamable: ru.TypeTag, ERR](
      consumerSettings: ConsumerSettings[F, String, Either[ERR, CC]],
      report: ERR => F[Unit]
  ): fs2.Stream[F, CC] =
    KafkaConsumer
      .stream[F, String, Either[ERR, CC]](consumerSettings)
      .subscribeTo(Streamable.getTopicName[CC])
      .records
      .evalMapFilter {
        _.record.value match {
          case Left(err)     => report(err).map(_ => none[CC])
          case Right(record) => record.some.pure[F]
        }

      }

  def reportfulKafkaStream[F[_]: Async: Concurrent: Console, CC <: Streamable: ru.TypeTag](
      consumerSettings: ConsumerSettings[F, String, Either[Error, CC]]
  ): fs2.Stream[F, CC] =
    reportfulKafkaStream[F, CC, Error](consumerSettings, { err: Error => Console[F].println(err.show) })
}
