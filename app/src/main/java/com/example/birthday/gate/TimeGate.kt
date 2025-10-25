package com.example.birthday.gate

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

object TimeGate {
    val zone: ZoneId = ZoneId.of("Europe/Madrid")

    private const val targetYear = 2025
    private const val targetMonth = 12
    private const val targetDay = 3

    var targetDate: LocalDateTime = initialTargetDate()

    private fun initialTargetDate(): LocalDateTime {
        return LocalDateTime.of(targetYear, targetMonth, targetDay, 0, 0)
    }

    fun isUnlocked(now: ZonedDateTime = ZonedDateTime.now(zone)): Boolean {
        return !now.isBefore(resolveTarget(now))
    }

    fun countdownFlow(): Flow<Duration> = flow {
        while (true) {
            val now = ZonedDateTime.now(zone)
            val target = resolveTarget(now)
            val duration = if (now.isBefore(target)) {
                Duration.between(now, target)
            } else {
                Duration.ZERO
            }
            emit(duration)
            if (duration.isZero || duration.isNegative) {
                break
            }
            delay(1000)
        }
    }

    private fun resolveTarget(now: ZonedDateTime): ZonedDateTime {
        val configured = targetDate
        if (
            now.year > configured.year &&
            configured.monthValue == targetMonth &&
            configured.dayOfMonth == targetDay &&
            configured.hour == 0 &&
            configured.minute == 0 &&
            configured.second == 0 &&
            configured.nano == 0
        ) {
            targetDate = configured.plusYears((now.year - configured.year).toLong())
        }
        return targetDate.atZone(zone)
    }
}
