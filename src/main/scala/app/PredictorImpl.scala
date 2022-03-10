package app


//import java.util.concurrent.atomic.AtomicReference

import test.model.{PredictRequest, PredictResponse, PredictorGrpc}

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}

class PredictorImpl
  extends PredictorGrpc.Predictor {

  private implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(4))

  private val model = LogRegModel(Array(1, 2, 3, 4, 5), 10)

  override def predict(request: PredictRequest): Future[PredictResponse] = Future {

    PredictResponse(model.getScore(request.vector.toArray))
  }
}

object PredictorImpl {

}
