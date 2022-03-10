package app



import test.model.{PredictRequest, PredictResponse, PredictorGrpc}

import scala.concurrent.Future

class PredictorImpl
  extends PredictorGrpc.Predictor
  {

  override def predict(request: PredictRequest): Future[PredictResponse] = Future.successful(PredictResponse(1))
}
