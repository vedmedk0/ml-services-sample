package generator

import cats._
import cats.effect._
import cats.effect.kernel.Sync
import cats.implicits.toFunctorOps
import config.MLServiceConfig
import domain._
import fs2.kafka.{GenericSerializer, ProducerSettings, Serializer}
import generator.emitter._
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._

object GeneratorService {

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

    val ec = c.emitterConfig

    implicit val modelProducerSink = Sink.kafkaSink(producerSettings[Model](ec.kafkaBootstrapServer))
    implicit val predictionProducerSink =
      Sink.kafkaSink(producerSettings[Prediction](ec.kafkaBootstrapServer))
    val me = new ModelEmitter(ec.emitModelPeriod)
    val pe = new PredictionEmitter(ec.emitPredictionPeriod)
    Emitter.emit(me, pe)
  }
}
