package app

import io.grpc.{Server, ServerBuilder}
import test.model.PredictorGrpc

import java.util.logging.Logger
import scala.concurrent.ExecutionContext

class PredictorService(config: Config)(implicit executionContext: ExecutionContext) {

  private val logger = Logger.getLogger(classOf[PredictorService].getName)

  private val port                 = config.port
  private[this] var server: Server = null

  def start(): Unit = {
    server = ServerBuilder
      .forPort(port)
      .addService(PredictorGrpc.bindService(new PredictorImpl(config), executionContext))
      .build
      .start
    logger.info("Server started, listening on " + port)
    sys.addShutdownHook {
      System.err.println("*** shutting down gRPC server since JVM is shutting down")
      stop()
      System.err.println("*** server shut down")
    }
  }

  def blockUntilShutdown(): Unit = {
    if (server != null) {
      server.awaitTermination()
    }
  }

  private def stop(): Unit = {
    if (server != null) {
      server.shutdown()
    }
  }

}
