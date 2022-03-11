package client

import app.Config
import io.grpc.ManagedChannelBuilder
import test.model.{ChangeModelRequest, PredictRequest, PredictorGrpc}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Random


/**
 * client for imitating input stream of messages and models
 */
class Client {

  private val config: Config = Config.read()

  private val rnd = new Random()

  private val models = List(
    ChangeModelRequest(Seq(6.08F, -7.78F, 6.34F, -8.05F, 3.14F), 61.35F),
    ChangeModelRequest(Seq(8.46F, -1.74F, 6.08F, -4.25F, 1.92F), 71.37F),
    ChangeModelRequest(Seq(6.53F, -5.46F, 0.0F, -9.95F, 6.29F), 43.3F),
    ChangeModelRequest(Seq(3.2F, -7.32F, 1.46F, -2.29F, 4.26F), 94.81F),
    ChangeModelRequest(Seq(2.71F, -0.82F, 8.54F, -0.21F, 2.1F), 66.25F),
  )

  private def generateRequest(): PredictRequest = {
    val vector = for (_ <- 1 to 5) yield rnd.nextFloat()
    val label = rnd.nextInt(2)
    PredictRequest(vector, label)
  }

  private val channel = ManagedChannelBuilder.forAddress("localhost", config.port).usePlaintext().build
  private val blockingStub = PredictorGrpc.blockingStub(channel)
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
    val sendDataF = sendData()

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