package emitters

import cats.effect._

object StreamEmitterApp extends IOApp.Simple {

  override val run: IO[Unit] = StreamEmitterService.run[IO]

}
