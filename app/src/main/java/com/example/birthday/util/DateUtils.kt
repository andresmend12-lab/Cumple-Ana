package com.example.birthday.util

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateUtils {
    private val photoFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")

    fun generatePhotoName(activityOrder: Int, zoneId: ZoneId = ZoneId.of("Europe/Madrid")): String {
        val timestamp = LocalDateTime.now(zoneId).format(photoFormatter)
        return "Ana_Act${'$'}activityOrder_${'$'}timestamp.jpg"
    }
}
