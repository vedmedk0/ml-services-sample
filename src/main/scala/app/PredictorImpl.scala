package app


//import java.util.concurrent.atomic.AtomicReference

import test.model._

import java.util.logging.{Level, Logger}
import scala.concurrent.{ExecutionContext, Future}

class PredictorImpl(implicit executionContext: ExecutionContext)
  extends PredictorGrpc.Predictor {

  private val model = LogRegModel(Array(1, 2, 3, 4, 5), 10)

  private val logger = Logger.getLogger(classOf[PredictorService].getName)



  // CHALLENGE NOTE: I decided to use here simple request-response api, but
  // streaming api with observer can also be used
  override def predict(request: PredictRequest): Future[PredictResponse] = Future {

    logger.log(Level.INFO, s"request with ${request.vector.mkString("[", ", ", "]")}")

    PredictResponse(model.getScore(request.vector.toArray))
  }

  override def changeModel(request: ChangeModelRequest): Future[ChangeModelResponse] = Future {
    ChangeModelResponse("not implemented")
  }
}

object PredictorImpl {

}
