package emitters

import cats.effect._
import cats.effect.std._
import cats.syntax.all._
import domain.Prediction
import fs2.kafka.{KafkaProducer, ProducerSettings}
import kafka.KafkaHelpers

import scala.concurrent.duration._

class PredictionEmitter[F[_]: Temporal: Async](
    producerSettings: ProducerSettings[F, String, Prediction],
    emitPeriod: FiniteDuration
) {

  private val random = Random.scalaUtilRandom[F]

  private val generateVector: F[Prediction] =
    for {
      rnd    <- random
      vector <- (for (_ <- 1 to 5) yield rnd.nextFloat).toVector.sequence
      label  <- rnd.nextIntBounded(2)
    } yield Prediction(vector, label.toFloat)

  private val vectorGenerator = fs2.Stream.eval(generateVector).repeat.metered(emitPeriod)

  def emitVectors() = vectorGenerator
    .evalMap {
      KafkaHelpers.objectToRecord[F, Prediction]
    }
    .through(KafkaProducer.pipe(producerSettings))

}
