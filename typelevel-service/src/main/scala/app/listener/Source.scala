package app.listener

import cats.Applicative
import cats.effect._
import cats.implicits._
import domain.Streamable
import fs2.kafka._
import io.circe.Error
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition

import scala.reflect.runtime.{universe => ru}

trait Source[F[_], +A, +ERR] {

  def listen: fs2.Stream[F, CommittableConsumerRecord[F, String, Either[ERR, A]]]

}

object Source {

  implicit def kafkaSourceCirceDeser[F[_]: Async, A <: Streamable: ru.TypeTag](
      consumerSettings: ConsumerSettings[F, String, Either[Error, A]]
  ): Source[F, A, Error] = new Source[F, A, Error] {
    override def listen: fs2.Stream[F, CommittableConsumerRecord[F, String, Either[Error, A]]] = KafkaConsumer
      .stream[F, String, Either[Error, A]](consumerSettings)
      .subscribeTo(Streamable.getTopicName[A])
      .records
  }

  implicit def fakeSource[F[_]: Async: Applicative, A](fakeRecords: Seq[A]): Source[F, A, String] =
    new Source[F, A, String] {
      override def listen: fs2.Stream[F, CommittableConsumerRecord[F, String, Either[String, A]]] =
        fs2.Stream.emits(fakeRecords).evalMap { fr =>
          val cr: ConsumerRecord[String, Either[String, A]] =
            ConsumerRecord("fake_topic", 1, 1, "fake_key", fr.asRight[String])
          val co =
            CommittableOffset[F](new TopicPartition("fake_topic", 1), new OffsetAndMetadata(1), None, _ => ().pure[F])
          CommittableConsumerRecord(cr, co).pure[F]
        } ++
          fs2.Stream.eval(
            CommittableConsumerRecord(
              ConsumerRecord("fake_topic", 1, 1, "fake_key", "error".asLeft[A]),
              CommittableOffset(new TopicPartition("fake_topic", 1), new OffsetAndMetadata(1), None, _ => ().pure[F])
            ).pure[F]
          )
    }

}
