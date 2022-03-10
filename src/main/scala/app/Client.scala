package app

import io.grpc.ManagedChannelBuilder
import test.model.{PredictRequest, PredictorGrpc}

import scala.util.Random


/**
 * client for imitating input stream of messages
 */
class Client {


  private val rnd = new Random()

  private def generateRequest(): PredictRequest = {
    val vector = (for (_ <- 1 to 5) yield rnd.nextFloat()).toArray
    val label = rnd.nextBoolean()
    PredictRequest(vector, label)
  }

  def run(): Unit = {
    val channel = ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext().build
    val blockingStub = PredictorGrpc.blockingStub(channel)


    for (_ <- 1 to 1000) yield {
      val result = blockingStub.predict(generateRequest())
      println(result)
      Thread.sleep(500)
    }

  }
}


object Client extends App {
  new Client().run()
}