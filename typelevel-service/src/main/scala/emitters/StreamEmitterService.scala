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

object StreamEmitterService {

  def run[F[_]: Async: Temporal]: F[Unit] = FlatMap[F].flatMap(MLServiceConfig.load()) { c =>
    //TODO: topic-name dependent serialization
    def caseClassSerializer[CC: Encoder]: Serializer[F, CC] =
      GenericSerializer
        .lift[F, CC] { cc: CC =>
          Sync[F].delay(cc.asJson.toString().getBytes("UTF-8"))
        }

    def producerSettings[CC: Encoder](kafkaBootstrapService: String) =
      ProducerSettings(keySerializer = GenericSerializer.string[F], valueSerializer = caseClassSerializer[CC])
        .withBootstrapServers(kafkaBootstrapService)

    def emit(modelEmitter: ModelEmitter[F], vectorEmitter: PredictionEmitter[F]): F[Unit] =
      (modelEmitter.emitModels() concurrently vectorEmitter.emitVectors()).compile.drain

    val ec = c.emitterConfig
    val me = new ModelEmitter[F](producerSettings[Model](ec.kafkaBootstrapServer), ec.emitModelPeriod)
    val pe = new PredictionEmitter[F](producerSettings[Prediction](ec.kafkaBootstrapServer), ec.emitPredictionPeriod)
    emit(me, pe)
  }
}
