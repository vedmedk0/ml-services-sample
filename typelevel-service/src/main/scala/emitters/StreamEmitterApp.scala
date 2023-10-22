package emitters

import cats._
import cats.effect._
import cats.effect.kernel.Sync
import config.MLServiceConfig
import domain._
import fs2.kafka.{GenericSerializer, ProducerSettings, Serializer}
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._

class StreamEmitterApp[F[_]: Async: Temporal] {

  private val config: F[MLServiceConfig] = MLServiceConfig.load()

  //TODO: topic-name dependent serialization
  private def caseClassSerializer[CC: Encoder]: Serializer[F, CC] =
    GenericSerializer
      .lift[F, CC] { cc: CC =>
        Sync[F].delay(cc.asJson.toString().getBytes("UTF-8"))
      }

  private def producerSettings[CC: Encoder](kafkaBootstrapService: String) =
    ProducerSettings(keySerializer = GenericSerializer.string[F], valueSerializer = caseClassSerializer[CC])
      .withBootstrapServers(kafkaBootstrapService)

  def run: F[Unit] = FlatMap[F].flatMap(config) { c =>
    val ec = c.emitterConfig
    val me = new ModelEmitter[F](producerSettings[Model](ec.kafkaBootstrapServer), ec.emitModelPeriod)
    val pe = new PredictionEmitter[F](producerSettings[Prediction](ec.kafkaBootstrapServer), ec.emitPredictionPeriod)
    val service = new StreamEmitterService[F](me, pe)
    service.emit
  }

}

object StreamEmitterApp extends IOApp.Simple {

  override val run: IO[Unit] = new StreamEmitterApp[IO].run

}
