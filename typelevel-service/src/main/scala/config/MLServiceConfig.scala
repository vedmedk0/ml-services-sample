package config

import cats.effect.kernel.Sync
import config.MLServiceConfig._
import fs2.kafka.AutoOffsetReset
import pureconfig.error.CannotConvert
import pureconfig.module.catseffect.syntax.CatsEffectConfigSource
import pureconfig.{ConfigReader, ConfigSource}
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._

import scala.concurrent.duration.FiniteDuration

case class MLServiceConfig(
    appConfig: AppConfig,
    emitterConfig: EmitterConfig
)

object MLServiceConfig {
  case class AppConfig(
      kafkaBootstrapServer: String,
      resetOffset: AutoOffsetReset,
      modelThreshold: Float,
      logPeriod: Int
  )

  case class EmitterConfig(kafkaBootstrapServer: String, emitPredictionPeriod: FiniteDuration, emitModelPeriod: FiniteDuration)

  implicit val offsetConfigReader: ConfigReader[AutoOffsetReset] = ConfigReader.fromString { s =>
    s.toLowerCase match {
      case "earliest" => Right(AutoOffsetReset.Earliest)
      case "latest"   => Right(AutoOffsetReset.Latest)
      case "none"     => Right(AutoOffsetReset.None)
      case _ => Left(CannotConvert(s, "AutoOffsetReset", "wrong string, should be one of: earliest, latest, none"))
    }
  }

  def load[F[_]:Sync](): F[MLServiceConfig] = ConfigSource.default.loadF[F, MLServiceConfig]

}
