package app

import cats.effect.{IO, IOApp}

import java.lang.Exception

object PredictorApp extends IOApp.Simple {

  override val run: IO[Unit] = PredictorService.run[IO].handleError(err => err.getMessage)

}
