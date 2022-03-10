package app

import pureconfig.error.ConfigReaderFailures
import pureconfig.generic.semiauto._
import pureconfig.{ConfigReader, ConfigSource}

import scala.util.{Failure, Success, Try}

final case class Config(parameter: String)

object Config {

  implicit val configReader = deriveReader[Config]

  def read(): Config =
    ConfigSource.default
      .value()
      .flatMap { source =>
        ConfigReader[Config].from(source)
      }
      .fold(crf => handleConfigReaderFailures(crf), cfg => Success(cfg))
      .get

  private def handleConfigReaderFailures(
      failures: ConfigReaderFailures
  ): Try[Config] = {
    System.err.println("Unable to read config:")
    System.err.println(failures.prettyPrint())
    Failure(new RuntimeException("error when reading config"))
  }
}
