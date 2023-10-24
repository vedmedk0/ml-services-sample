package generator

import cats.effect._

object GeneratorApp extends IOApp.Simple {

  override val run: IO[Unit] = GeneratorService.run[IO]

}
