package com.github.BambooTuna.CircuitBreakOrder

import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, ActorSystem, OneForOneStrategy, Props}
import akka.stream.ActorMaterializer
import com.github.BambooTuna.CircuitBreakOrder.GetCircuitBreakInfoActor.{GetCircuitBreakInfoData, Reboot}
import com.github.BambooTuna.CircuitBreakOrder.GetCircuitBreakInfoScheduleActor._
import com.github.BambooTuna.CryptoLib.restAPI.client.bitflyer.APIList.GetCircuitBreakInfoResponse
import com.github.BambooTuna.CryptoLib.restAPI.client.bitflyer.BitflyerRestAPIs
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration._

class GetCircuitBreakInfoScheduleActor(bitflyerRestAPIs: BitflyerRestAPIs) extends Actor {

  implicit val system: ActorSystem = context.system
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  val logger = LoggerFactory.getLogger(getClass)

  val getCircuitBreakInfoActor = context.actorOf(Props(classOf[GetCircuitBreakInfoActor], bitflyerRestAPIs), GetCircuitBreakInfoActor.ActorName)

  val orderInterval = 1.minutes
  val timerTimeout = 5.minutes
  val timerInterval = 1.minutes
  val timer = system.scheduler.schedule(timerInterval,  timerInterval, self, AddTimerCount)

  var timerCount = 0.minutes

  def receive = {
    case Start =>
      getCircuitBreakInfoActor ! GetCircuitBreakInfoData
    case d: GetCircuitBreakInfoResponse =>
      context.parent ! d
      timerCount = 0.minutes
      Future{Thread.sleep(orderInterval.toMillis)}.onComplete(_ => self ! Start)
    case AddTimerCount =>
      timerCount = timerCount.plus(timerInterval)
      if (timerCount > timerTimeout) {
        timerCount = 0.minutes
        getCircuitBreakInfoActor ! Reboot
      }
    case other => logger.debug(other.toString)
  }

  override def preStart() = {
    super.preStart()
    self ! Start
  }

  override def postStop() = {
    super.postStop()
    timer.cancel()
  }

  override def supervisorStrategy = OneForOneStrategy() {
    case _ =>
      Thread.sleep(5000)
      Restart
  }

}

object GetCircuitBreakInfoScheduleActor {

  val ActorName = "GetCircuitBreakInfoScheduleActor"

  sealed trait Receive
  case object Start extends Internal

  sealed trait Internal
  case class RevertToParent(data: GetCircuitBreakInfoResponse) extends Internal
  case object AddTimerCount extends Internal

  private case class InternalException(e: String) extends Exception(e)

}


