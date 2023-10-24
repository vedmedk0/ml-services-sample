package client

import app.Config
import io.grpc.ManagedChannelBuilder
import test.model.{ChangeModelRequest, PredictRequest, PredictorGrpc}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Random

/** client for imitating input stream of messages and models
  */
class Client {

  private val config: Config = Config.read()

  private val rnd = new Random()

  private val models = List(
    ChangeModelRequest(Seq(6.08f, -7.78f, 6.34f, -8.05f, 3.14f), 61.35f),
    ChangeModelRequest(Seq(8.46f, -1.74f, 6.08f, -4.25f, 1.92f), 71.37f),
    ChangeModelRequest(Seq(6.53f, -5.46f, 0.0f, -9.95f, 6.29f), 43.3f),
    ChangeModelRequest(Seq(3.2f, -7.32f, 1.46f, -2.29f, 4.26f), 94.81f),
    ChangeModelRequest(Seq(2.71f, -0.82f, 8.54f, -0.21f, 2.1f), 66.25f)
  )

  private def generateRequest(): PredictRequest = {
    val vector = for (_ <- 1 to 5) yield rnd.nextFloat()
    val label  = rnd.nextInt(2)
    PredictRequest(vector, label)
  }

  private val channel         = ManagedChannelBuilder.forAddress("localhost", config.port).usePlaintext().build
  private val blockingStub    = PredictorGrpc.blockingStub(channel)
  private val nonBlockingStub = PredictorGrpc.stub(channel)

  private def sendModels(): Future[Unit] = Future {
    models.map { model =>
      blockingStub.changeModel(model)
      Thread.sleep(60 * 1000)
    }
  }

  private def sendData(): Future[Unit] = Future {
    for (_ <- 1 to config.inputDataCount) yield {
      val res = nonBlockingStub.predict(generateRequest())
      Thread.sleep(500)
      res.recover { case e => println(e.toString) }
    }
  }

  def run(): Unit = {

    val sendModelsF = sendModels()
    val sendDataF   = sendData()

    val result = for {
      _ <- sendDataF
      _ <- sendModelsF
    } yield ()

    Await.result(result, Duration.Inf)

  }
}

object Client extends App {
  new Client().run()
}
