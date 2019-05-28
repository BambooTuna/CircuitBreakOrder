package com.github.BambooTuna.CircuitBreakOrder

import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.ActorMaterializer
import com.github.BambooTuna.CircuitBreakOrder.CheckExecutionsActor.CheckExecutionsByOrderId
import com.github.BambooTuna.CircuitBreakOrder.DiscordPushNotification.DiscordPush
import com.github.BambooTuna.CircuitBreakOrder.MainLogicActor.PushNotification
import com.github.BambooTuna.CircuitBreakOrder.Protocol.MainLogicSettingOptions
import com.github.BambooTuna.CryptoLib.restAPI.client.bitflyer.APIList.BitflyerEnumDefinition.{OrderType, Side}
import com.github.BambooTuna.CryptoLib.restAPI.client.bitflyer.APIList._
import com.github.BambooTuna.CryptoLib.restAPI.model.Entity
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContextExecutor
import io.circe.generic.auto._

import scala.util.{Failure, Success}


class MainLogicActor(mainLogicSettingOptions: MainLogicSettingOptions) extends Actor {

  implicit val system: ActorSystem = context.system
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  val logger = LoggerFactory.getLogger(getClass)
  /////
  val bitflyerRestAPIs = mainLogicSettingOptions.bitflyerRestAPIs
  val discordRestAPIs = mainLogicSettingOptions.discordRestAPIs
  val size = mainLogicSettingOptions.orderOptions.size
  val priceDelta = mainLogicSettingOptions.orderOptions.priceDelta
  /////

  val getCircuitBreakInfoScheduleActor = context.actorOf(Props(classOf[GetCircuitBreakInfoScheduleActor], bitflyerRestAPIs), GetCircuitBreakInfoScheduleActor.ActorName)
  val checkExecutionsActor = context.actorOf(Props(classOf[CheckExecutionsActor], bitflyerRestAPIs), CheckExecutionsActor.ActorName)
  val discordPushNotification = context.actorOf(Props(classOf[DiscordPushNotification], discordRestAPIs), DiscordPushNotification.ActorName)

  def receive = {
    case GetCircuitBreakInfoResponse(data) => order(data)
    case PushNotification(m) => discordPushNotification ! DiscordPush(m)
    case other => logger.info(other.toString)
  }

  def order(circuitBreakInfoData: CircuitBreakInfoData): Unit = {
    bitflyerRestAPIs.simpleOrder.run(
      entity = Some(
        Entity(
          SimpleOrderBody(
            product_code = "FX_BTC_JPY",
            child_order_type = OrderType.Limit,
            side = Side.Buy,
            price = circuitBreakInfoData.lower_limit.longValue() + priceDelta,
            size = size,
            minute_to_expire = 1L
          )
        )
      )
    ).onComplete{
      case Success(v) => v.fold(e => self ! PushNotification(e.bodyString), s => checkExecutionsActor ! CheckExecutionsByOrderId(s.child_order_acceptance_id))
      case Failure(exception) => self ! PushNotification(exception.getMessage)
    }

    bitflyerRestAPIs.simpleOrder.run(
      entity = Some(
        Entity(
          SimpleOrderBody(
            product_code = "FX_BTC_JPY",
            child_order_type = OrderType.Limit,
            side = Side.Sell,
            price = circuitBreakInfoData.upper_limit.longValue() - priceDelta,
            size = size,
            minute_to_expire = 1L
          )
        )
      )
    ).onComplete{
      case Success(v) => v.fold(e => self ! PushNotification(e.bodyString), s => checkExecutionsActor ! CheckExecutionsByOrderId(s.child_order_acceptance_id))
      case Failure(exception) => self ! PushNotification(exception.getMessage)
    }
  }

}

object MainLogicActor {

  val ActorName = "MainLogicActor"

  sealed trait Receive
  case class PushNotification(message: String) extends Receive

}
