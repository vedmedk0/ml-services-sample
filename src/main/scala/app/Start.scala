package app


class Start {

  val config: Config = Config.read()


  def run(): Unit = {
    println(s"${config.parameter}")
  }
}

object Start extends App {
  new Start().run()
}
