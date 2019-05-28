package com.github.BambooTuna.CircuitBreakOrder

import com.github.BambooTuna.CryptoLib.restAPI.client.bitflyer.BitflyerRestAPIs
import com.github.BambooTuna.CryptoLib.restAPI.client.discord.DiscordRestAPIs

object Protocol {

  case class MainLogicSettingOptions(bitflyerRestAPIs: BitflyerRestAPIs, discordRestAPIs: DiscordRestAPIs, orderOptions: OrderOptions)

  case class OrderOptions(size: BigDecimal, priceDelta: Long)

}
