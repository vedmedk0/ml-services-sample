package app

import scala.concurrent.ExecutionContext


class StartServer {

  val config: Config = Config.read()

  //CHALLENGE NOTE: generally global executionContext but in real app
  // pools for service and server can be separated
  implicit val executionContext = ExecutionContext.global

  def run(): Unit = {
    val server = new PredictorService(config)
    server.start()
    server.blockUntilShutdown()
  }
}

object StartServer extends App {
  new StartServer().run()
}
