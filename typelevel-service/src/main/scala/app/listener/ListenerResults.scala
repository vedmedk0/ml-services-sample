package app.listener

import app.listener.PredictionListener.AccuracyCounter
import cats.Show
import domain.{Model, Prediction}
import io.circe.Error

object ListenerResults {

  sealed trait ReportDensity

  object ReportDensity {
    case object Sparse extends ReportDensity

    case object Frequent extends ReportDensity

  }

  sealed trait PredictorResult {
    def message: String
  }

  trait PredictorError extends PredictorResult
  case class NoModelError() extends PredictorError {
    override val message: String = "No model set"
  }

  case class DimensionsNotEqualError(prediction: Prediction, model: Model) extends PredictorError {
    override val message: String =
      s"Dimensions not equal: ${prediction.vector.length} for prediction and ${model.weights.length} for model"
  }

  case class ParseError(error: Error) extends PredictorError {
    override val message: String = s"Parse error: ${error.getMessage}"
  }

  case class OtherError(message: String) extends PredictorError

  trait PredictorSuccess extends PredictorResult {
    def reportDensity: ReportDensity = ReportDensity.Sparse
  }

  case class Report(ac: AccuracyCounter) extends PredictorSuccess {
    override def message: String = s"Result: $ac, accuracy:${ac.accuracy}"

    override def reportDensity: ReportDensity = ReportDensity.Frequent
  }

  case class ChangedModel(oldModel: Model, newModel: Model) extends PredictorSuccess {
    override def message: String = s"Changed model $oldModel to $newModel"
  }

  case class InitModel(model: Model) extends PredictorSuccess {
    override def message: String = s"Init model $model"
  }

  implicit val showResult: Show[PredictorResult] = (t: PredictorResult) => t.message

}
