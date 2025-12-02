package com.example.birthday.util

import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object TimeUtils {
    val zoneId: ZoneId = ZoneId.of("Europe/Madrid")
    private val hourMinuteFormatter = DateTimeFormatter.ofPattern("HH:mm")

    // Configuración del cumpleaños
    private const val BIRTHDAY_MONTH = 12
    private const val BIRTHDAY_DAY = 3

    fun now(zone: ZoneId = zoneId): ZonedDateTime = ZonedDateTime.now(zone)

    /**
     * Calcula la fecha del próximo cumpleaños.
     * Si hoy es el cumpleaños o ya pasó este año, devuelve la fecha del año siguiente.
     */
    fun getNextBirthday(): ZonedDateTime {
        val now = now()
        val thisYearBirthday = ZonedDateTime.of(now.year, BIRTHDAY_MONTH, BIRTHDAY_DAY, 0, 0, 0, 0, zoneId)

        return if (now.isBefore(thisYearBirthday)) {
            thisYearBirthday
        } else {
            // Si ya pasó o es hoy, apuntamos al del año siguiente
            thisYearBirthday.plusYears(1)
        }
    }

    fun formatDuration(duration: Duration): String {
        val normalized = if (duration.isNegative) Duration.ZERO else duration.truncatedTo(ChronoUnit.SECONDS)
        val days = normalized.toDays()
        val hours = normalized.toHours() % 24
        val minutes = (normalized.toMinutes() % 60)
        val seconds = (normalized.seconds % 60)

        // Si hay más de 24h mostramos los días, si no, formato reloj
        return if (days > 0) {
            String.format("%dd %02d:%02d:%02d", days, hours, minutes, seconds)
        } else {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }
    }

    fun formatUnlockTime(unlockAt: ZonedDateTime): String =
        unlockAt.withZoneSameInstant(zoneId).toLocalTime().format(hourMinuteFormatter)
}