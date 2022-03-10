package app

import scala.concurrent.ExecutionContext


class Start {

  val config: Config = Config.read()


  def run(): Unit = {
    val server = new HelloWorldServer(ExecutionContext.global)
    server.start()
    server.blockUntilShutdown()
  }
}

object Start extends App {
  new Start().run()


}
