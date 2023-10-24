package emitters

import cats.effect._
import cats.effect.std._
import cats.syntax.all._
import domain.Prediction
import fs2.kafka.ProducerResult

import scala.concurrent.duration._

class PredictionEmitter[F[_]: Temporal: Async](
    emitPeriod: FiniteDuration
)(implicit sink: Sink[F, Prediction, ProducerResult[String, Prediction]])
    extends Emitter[F, Prediction, ProducerResult[String, Prediction]] {

  override val generate = fs2.Stream
    .eval(for {
      rnd    <- Random.scalaUtilRandom[F]
      vector <- (for (_ <- 1 to 5) yield rnd.nextFloat).toVector.sequence
      label  <- rnd.nextIntBounded(2)
    } yield Prediction(vector, label.toFloat))
    .repeat
    .metered(emitPeriod)

}
