package com.github.BambooTuna.CircuitBreakOrder

import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import com.github.BambooTuna.CircuitBreakOrder.MainLogicActor.PushNotification
import com.github.BambooTuna.CircuitBreakOrder.Protocol.{MainLogicSettingOptions, OrderOptions}
import com.github.BambooTuna.CryptoLib.restAPI.client.bitflyer.BitflyerRestAPIs
import com.github.BambooTuna.CryptoLib.restAPI.client.discord.DiscordRestAPIs
import com.github.BambooTuna.CryptoLib.restAPI.model.ApiKey
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContextExecutor

object Main extends App {

  val rootConfig: Config = ConfigFactory.load()

  implicit val system: ActorSystem                        = ActorSystem("CircuitBreakOrder", config = rootConfig)
  implicit val materializer: ActorMaterializer            = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  val logger = LoggerFactory.getLogger(getClass)

  val bitflyerRestAPIs = new BitflyerRestAPIs(ApiKey(rootConfig.getString("apiKey.bitflyer_key"), rootConfig.getString("apiKey.bitflyer_secret")))
  val discordRestAPIs = new DiscordRestAPIs(ApiKey(rootConfig.getString("apiKey.discord_key"), rootConfig.getString("apiKey.discord_secret")))

  val size = BigDecimal(rootConfig.getString("order.size"))
  val priceDelta = rootConfig.getLong("order.price_delta")

  val mainLogicActor = system.actorOf(Props(classOf[MainLogicActor], MainLogicSettingOptions(
    bitflyerRestAPIs = bitflyerRestAPIs,
    discordRestAPIs = discordRestAPIs,
    OrderOptions(size, priceDelta)
  )), MainLogicActor.ActorName)

  mainLogicActor ! PushNotification("========== CircuitBreakOrder start ==========")
  logger.info("========== CircuitBreakOrder start ==========")

  sys.addShutdownHook {
    mainLogicActor ! PushNotification("========== CircuitBreakOrder Shutdown ==========")
    logger.info("========== CircuitBreakOrder Shutdown ==========")
  }

}
