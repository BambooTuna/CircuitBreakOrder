package com.github.BambooTuna.CircuitBreakOrder

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import akka.actor.{Actor, ActorSystem}
import akka.stream.ActorMaterializer
import com.github.BambooTuna.CircuitBreakOrder.CheckExecutionsActor.{CheckExecutionsByOrderId, RevertPushToParent}
import com.github.BambooTuna.CircuitBreakOrder.MainLogicActor.PushNotification
import com.github.BambooTuna.CryptoLib.restAPI.client.bitflyer.APIList.BitflyerEnumDefinition.Side
import com.github.BambooTuna.CryptoLib.restAPI.client.bitflyer.APIList.GetExecutionsQueryParameters
import com.github.BambooTuna.CryptoLib.restAPI.client.bitflyer.BitflyerRestAPIs
import com.github.BambooTuna.CryptoLib.restAPI.model.QueryParameters
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContextExecutor, Future}
import io.circe.generic.auto._


class CheckExecutionsActor(bitflyerRestAPIs: BitflyerRestAPIs) extends Actor {

  implicit val system: ActorSystem = context.system
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  val logger = LoggerFactory.getLogger(getClass)

  def receive = {
    case CheckExecutionsByOrderId(id) => getExecutions(id)
    case RevertPushToParent(m) => context.parent ! PushNotification(m)
    case other => logger.debug(other.toString)
  }

  def getExecutions(id: String) = {

    for {
      _ <- Future{Thread.sleep(90000)}
      res <- bitflyerRestAPIs.getExecutions.run(
        queryParameters = Some(
          QueryParameters(
            GetExecutionsQueryParameters(
              child_order_acceptance_id = id
            )
          )
        )
      )
      if res.isRight
      if res.right.get.nonEmpty
    } yield {
      logger.info(res.right.get.toString())
      self ! RevertPushToParent(res.right.get.foldLeft("CB指値約定情報")((l, r) => l + s"\n時刻：${convertDate(r.exec_date)}　価格：${r.price}　サイズ：${r.size * (if (r.side == Side.Buy) 1 else -1)}"))
    }

  }

  def convertDate(execDate: String) = {
    ZonedDateTime.parse(execDate + "+00:00").format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
  }

//  def convert(list: List[GetExecutionsResponse]) = {
//
//    list.foldLeft(
//      (
//        BigDecimal(0),
//        BigDecimal(0)
//      )
//    )((l, r) => {
//      val size = r.size * (if (r.side == Side.Buy) 1 else -1)
//
//      (l._1 + size ,l._2 + r.price * size)
//    })
//
//  }

}

object CheckExecutionsActor {

  val ActorName = "CheckExecutionsActor"

  sealed trait Receive
  case class CheckExecutionsByOrderId(id: String) extends Receive

  sealed trait Internal
  case class RevertPushToParent(message: String) extends Internal

}