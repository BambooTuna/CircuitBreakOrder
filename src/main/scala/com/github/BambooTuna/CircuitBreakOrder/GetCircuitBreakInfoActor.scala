package com.github.BambooTuna.CircuitBreakOrder

import akka.actor.{Actor, ActorSystem}
import akka.stream.ActorMaterializer
import com.github.BambooTuna.CircuitBreakOrder.GetCircuitBreakInfoActor._
import com.github.BambooTuna.CryptoLib.restAPI.client.bitflyer.APIList.{GetCircuitBreakInfoQueryParameters, GetCircuitBreakInfoResponse}
import com.github.BambooTuna.CryptoLib.restAPI.client.bitflyer.BitflyerRestAPIs
import com.github.BambooTuna.CryptoLib.restAPI.model.QueryParameters
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContextExecutor
import io.circe.generic.auto._

import scala.util.{Failure, Success}

class GetCircuitBreakInfoActor(bitflyerRestAPIs: BitflyerRestAPIs) extends Actor {

  implicit val system: ActorSystem = context.system
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  val logger = LoggerFactory.getLogger(getClass)

  def receive = {
    case GetCircuitBreakInfoData =>
      bitflyerRestAPIs.getCircuitBreakInfo.run(
        queryParameters = Some(
          QueryParameters(
            GetCircuitBreakInfoQueryParameters()
          )
        )
      ).onComplete{
        case Success(value) =>
          value.fold(e => self ! InternalException(e.bodyString), d => self ! RevertToParent(d))
        case Failure(exception) => self ! InternalException(exception.getMessage)
      }
    case RevertToParent(d) => context.parent ! d
    case e: InternalException =>
      logger.error(e.errorMessage)
      throw e
    case Reboot => self ! InternalException(s"Force Reboot by ${sender()}")
    case other => logger.info(other.toString)
  }

}

object GetCircuitBreakInfoActor {

  val ActorName = "GetCircuitBreakInfoActor"

  sealed trait Receive
  case object Reboot extends Internal

  sealed trait Internal
  case object GetCircuitBreakInfoData extends Internal
  case class RevertToParent(data: GetCircuitBreakInfoResponse) extends Internal

  private case class InternalException(errorMessage: String) extends Exception(errorMessage)

}