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

class PredictorApp[F[_]: Async: Concurrent: Console] {

  //TODO: topic-name dependent deserialization
  private def caseClassDeserializer[CC <: Streamable: Decoder]: Deserializer[F, Either[Error, CC]] =
    GenericDeserializer
      .lift[F, Either[Error, CC]] { bytes: Array[Byte] =>
        Sync[F].delay(decode[CC](new String(bytes, Charset.forName("UTF-8"))))
      }

  private def consumerSettings[CC <: Streamable: Decoder](
      bootstrapServer: String,
      autoOffsetReset: AutoOffsetReset,
      groupId: String
  ): ConsumerSettings[F, String, Either[Error, CC]] =
    ConsumerSettings(GenericDeserializer.string[F], caseClassDeserializer[CC])
      .withBootstrapServers(bootstrapServer)
      .withAutoOffsetReset(autoOffsetReset)
      .withGroupId(groupId)

  def run(): F[Unit] = FlatMap[F].flatMap(MLServiceConfig.load()) { c =>
    val ac = c.appConfig
    val pc = consumerSettings[Prediction](ac.kafkaBootstrapServer, ac.resetOffset, "predictionConsumer")
    val mc = consumerSettings[Model](ac.kafkaBootstrapServer, ac.resetOffset, "modelConsumer")
    new PredictorService[F](pc, mc, ac).runService()
  }
}

object PredictorApp extends IOApp.Simple {

  override val run: IO[Unit] = new PredictorApp[IO]().run()

}
