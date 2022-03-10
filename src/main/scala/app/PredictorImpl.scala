package app


//import java.util.concurrent.atomic.AtomicReference

import test.model._

import java.util.concurrent.atomic.{AtomicLong, AtomicReference}
import java.util.logging.{Level, Logger}
import scala.concurrent.{ExecutionContext, Future}
import scala.math.abs

class PredictorImpl(config: Config)(implicit executionContext: ExecutionContext)
  extends PredictorGrpc.Predictor {


  private val modelRef: AtomicReference[Option[LogRegModel]] = new AtomicReference[Option[LogRegModel]](None)

  private val logger = Logger.getLogger(classOf[PredictorService].getName)

  private val modelThreshold = config.port


  private val processedRecords = new AtomicLong(0)

  private val guessedRecords = new AtomicLong(0)

  private val passedRecordsToLog = new AtomicLong(0)


  // CHALLENGE NOTE: I decided to use here simple request-response api, but
  // streaming api with observer can also be used
  override def predict(request: PredictRequest): Future[PredictResponse] = Future {

    val model = modelRef.get().getOrElse(throw new RuntimeException("Service has not model set!"))

    val score = model.getScore(request.vector.toArray)
    val guessedRight = abs(score - request.classLabel) < modelThreshold


    processedRecords.getAndIncrement()
    if (guessedRight) guessedRecords.getAndIncrement()

    if (passedRecordsToLog.getAndIncrement() >= config.logPeriod) {
      logger.log(Level.INFO, s"current accuracy: ${guessedRecords.get().toFloat / processedRecords.get()}")
      passedRecordsToLog.set(0)
    }

    PredictResponse(score)
  }

  override def changeModel(request: ChangeModelRequest): Future[ChangeModelResponse] = Future {
    val newModel = LogRegModel(request.weights.toArray,request.bias)
    modelRef.set(Some(newModel))
    logger.log(Level.INFO,"new model: ")
    ChangeModelResponse("changed model successfully")
  }
}

object PredictorImpl {

}
