package emitters

import cats.effect.Temporal
import cats.effect.kernel.Async
import domain.Model
import fs2.kafka._
import kafka.KafkaHelpers

import scala.concurrent.duration.{DurationInt, FiniteDuration}

class ModelEmitter[F[_]: Temporal: Async](producerSettings: ProducerSettings[F, String, Model],emitPeriod: FiniteDuration) {

  private val modelGenerator =
    fs2
      .Stream[F, Model](
        Model(Vector(6.08f, -7.78f, 6.34f, -8.05f, 3.14f), 61.35f),
        Model(Vector(8.46f, -1.74f, 6.08f, -4.25f, 1.92f), 71.37f),
        Model(Vector(6.53f, -5.46f, 0.0f, -9.95f, 6.29f), 43.3f),
        Model(Vector(3.2f, -7.32f, 1.46f, -2.29f, 4.26f), 94.81f),
        Model(Vector(2.71f, -0.82f, 8.54f, -0.21f, 2.1f), 66.25f)
      )
      .repeat
      .metered(emitPeriod)
      .delayBy(5.second)

  def emitModels() =
    modelGenerator
      .evalMap { KafkaHelpers.objectToRecord[F, Model] }
      .through(KafkaProducer.pipe(producerSettings))


}
