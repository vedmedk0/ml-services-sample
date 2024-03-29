package app

import scala.concurrent.ExecutionContext

class StartServer {

  private val config: Config = Config.read()

  // CHALLENGE NOTE: generally global executionContext is not acceptable, but in real app
  // pools for service and server can be explicitly defined and separated
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
