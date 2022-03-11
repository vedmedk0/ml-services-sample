package app

import test.model._

import java.util.concurrent.atomic.{AtomicLong, AtomicReference}
import java.util.logging.{Level, Logger}
import scala.concurrent.{ExecutionContext, Future}
import scala.math.abs

class PredictorImpl(config: Config)(implicit executionContext: ExecutionContext)
  extends PredictorGrpc.Predictor {

  private val modelRef: AtomicReference[Option[LogRegModel]] = new AtomicReference[Option[LogRegModel]](None)

  private val logger = Logger.getLogger(classOf[PredictorService].getName)

  private val processedRecords = new AtomicLong(0)

  private val guessedRecords = new AtomicLong(0)

  private val passedRecordsToLog = new AtomicLong(0)

  /**
   * Get score of a sample from current model
   *
   * CHALLENGE NOTE: I decided to use here simple request-response gRPC api, but
   * streaming api with observer can also be used (or fs2/akka streams)
   *
   * @param request request with sample values and class label
   * @return result of prediction by current model
   */
  override def predict(request: PredictRequest): Future[PredictResponse] = Future {

    val model = modelRef.get().getOrElse {
      logger.log(Level.WARNING, "Client tried to use server without model set")
      throw new RuntimeException("Model has not been set!")
    }

    val score = model.getScore(request.vector.toVector)

    processStatistics(score, request.classLabel)

    PredictResponse(score)
  }

  /**
   * Change model in service
   *
   * @param request weights and biases of new model
   * @return text response if done successfully
   */
  override def changeModel(request: ChangeModelRequest): Future[ChangeModelResponse] = Future {
    val newModel = LogRegModel(request.weights.toVector, request.bias)
    modelRef.set(Some(newModel))
    logger.log(Level.INFO, s"new model: ${newModel.toString}")
    ChangeModelResponse("changed model successfully")
  }

  /**
   * Process prediction statistics and log
   *
   * @param score score from model
   * @param label real label for sample
   */
  private def processStatistics(score: Float, label: Float): Unit = {
    val guessedRight = abs(score - label) < config.modelThreshold

    processedRecords.getAndIncrement()
    if (guessedRight) guessedRecords.getAndIncrement()

    if (passedRecordsToLog.getAndIncrement() >= config.logPeriod) {
      logger.log(Level.INFO, s"Current accuracy: ${guessedRecords.get().toFloat / processedRecords.get()}")
      passedRecordsToLog.set(0)
    }
  }


}