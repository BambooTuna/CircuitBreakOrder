package com.github.BambooTuna.CircuitBreakOrder

import akka.actor.{Actor, ActorSystem}
import akka.stream.ActorMaterializer
import com.github.BambooTuna.CircuitBreakOrder.DiscordPushNotification.DiscordPush
import com.github.BambooTuna.CryptoLib.restAPI.client.discord.APIList.WebhookBody
import com.github.BambooTuna.CryptoLib.restAPI.client.discord.DiscordRestAPIs
import com.github.BambooTuna.CryptoLib.restAPI.model.Entity
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContextExecutor

import io.circe.generic.auto._

class DiscordPushNotification(discordRestAPIs: DiscordRestAPIs) extends Actor {

  implicit val system: ActorSystem = context.system
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  val logger = LoggerFactory.getLogger(getClass)

  def receive = {
    case DiscordPush(m) => sendMessage(m)
    case other => logger.debug(other.toString)
  }

  def sendMessage(message: String) = {
    discordRestAPIs.webhook.run(
      entity = Some(
        Entity(
          WebhookBody(
            username = "Bot",
            content = message
          )
        )
      )
    )
  }

}

object DiscordPushNotification {

  val ActorName = "DiscordPushNotification"

  sealed trait Receive
  case class DiscordPush(message: String) extends Receive

}