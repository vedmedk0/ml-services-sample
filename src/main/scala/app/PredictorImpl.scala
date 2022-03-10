package app


//import java.util.concurrent.atomic.AtomicReference

import test.model._

import java.util.concurrent.atomic.AtomicLong
import java.util.logging.{Level, Logger}
import scala.concurrent.{ExecutionContext, Future}
import scala.math.abs

class PredictorImpl(implicit executionContext: ExecutionContext)
  extends PredictorGrpc.Predictor {


  private val model = LogRegModel(Array(1, -2, 3, -4, 5), -1)

  private val logger = Logger.getLogger(classOf[PredictorService].getName)

  private val modelThreshold = 0.2 // TODO: to config


  private val processedRecords = new AtomicLong(0)

  private val guessedRecords = new AtomicLong(0)

  private val passedRecordsToLog = new AtomicLong(0)


  // CHALLENGE NOTE: I decided to use here simple request-response api, but
  // streaming api with observer can also be used
  override def predict(request: PredictRequest): Future[PredictResponse] = Future {

    val score = model.getScore(request.vector.toArray)
    val guessedRight = abs(score - request.classLabel) < modelThreshold


    processedRecords.getAndIncrement()
    if (guessedRight) guessedRecords.getAndIncrement()

    if (passedRecordsToLog.getAndIncrement() >= 10) {
      logger.log(Level.INFO, s"current accuracy: ${guessedRecords.get().toFloat / processedRecords.get()}")
      passedRecordsToLog.set(0)
    }

    PredictResponse(score)
  }

  override def changeModel(request: ChangeModelRequest): Future[ChangeModelResponse] = Future {
    ChangeModelResponse("not implemented")
  }
}

object PredictorImpl {

}
