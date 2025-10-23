package com.example.birthday.util

import java.time.Duration
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.ZonedDateTime

object TimeUtils {
    val zoneId: ZoneId = ZoneId.of("Europe/Madrid")

    private val hourMinuteFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun now(zone: ZoneId = zoneId): ZonedDateTime = ZonedDateTime.now(zone)

    fun formatDuration(duration: Duration): String {
        val normalized = if (duration.isNegative) Duration.ZERO else duration.truncatedTo(ChronoUnit.SECONDS)
        val hours = normalized.toHours()
        val minutes = (normalized.toMinutes() % 60)
        val seconds = (normalized.seconds % 60)
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    fun formatUnlockTime(unlockAt: ZonedDateTime): String = unlockAt.withZoneSameInstant(zoneId).toLocalTime().format(hourMinuteFormatter)
}
