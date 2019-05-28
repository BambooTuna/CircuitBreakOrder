package com.github.BambooTuna.CircuitBreakOrder

import java.time.ZoneId
import java.time.format.DateTimeFormatter

object SystemSetting {
  val timeZone = ZoneId.of("Asia/Tokyo")

  val timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

}
