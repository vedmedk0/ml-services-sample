package emitters

import cats.effect._

object StreamEmitterService {
  def emit[F[_]: Concurrent](modelEmitter: ModelEmitter[F], vectorEmitter: PredictionEmitter[F]): F[Unit] =
    (modelEmitter.emitModels() concurrently vectorEmitter.emitVectors()).compile.drain
}
