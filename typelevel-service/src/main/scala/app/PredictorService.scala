package app

import cats._
import cats.effect._
import cats.effect.std.Console
import config.MLServiceConfig
import domain._
import fs2.kafka._
import io.circe._
import io.circe.generic.auto._
import io.circe.parser.decode

import java.nio.charset.Charset

object PredictorService {

  def run[F[_]: Async: Concurrent: Console]: F[Unit] = FlatMap[F].flatMap(MLServiceConfig.load()) { c =>
    //TODO: topic-name dependent deserialization

    def caseClassDeserializer[CC <: Streamable: Decoder]: Deserializer[F, Either[Error, CC]] =
      GenericDeserializer
        .lift[F, Either[Error, CC]] { bytes: Array[Byte] =>
          Sync[F].delay(decode[CC](new String(bytes, Charset.forName("UTF-8"))))
        }

    def consumerSettings[CC <: Streamable: Decoder](
        bootstrapServer: String,
        autoOffsetReset: AutoOffsetReset,
        groupId: String
    ): ConsumerSettings[F, String, Either[Error, CC]] =
      ConsumerSettings(GenericDeserializer.string[F], caseClassDeserializer[CC])
        .withBootstrapServers(bootstrapServer)
        .withAutoOffsetReset(autoOffsetReset)
        .withGroupId(groupId)
    val ac = c.appConfig
    val pc = consumerSettings[Prediction](ac.kafkaBootstrapServer, ac.resetOffset, "predictionConsumer")
    val mc = consumerSettings[Model](ac.kafkaBootstrapServer, ac.resetOffset, "modelConsumer")
    Predictor.predict[F](pc, mc, ac)
  }

}
