package app

import cats.effect.{IO, IOApp}

object PredictorApp extends IOApp.Simple {

  override val run: IO[Unit] = PredictorService.run[IO]

}
