package emitters

import cats.effect._

class StreamEmitterService[F[_]: Concurrent](modelEmitter: ModelEmitter[F], vectorEmitter: PredictionEmitter[F]) {
  def emit: F[Unit] = (modelEmitter.emitModels() concurrently vectorEmitter.emitVectors()).compile.drain
}
