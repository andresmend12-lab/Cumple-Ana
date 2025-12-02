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

    var targetDate: LocalDateTime = LocalDateTime.of(2025, 12, 3, 0, 0)

    private fun nextTargetDate(now: ZonedDateTime): ZonedDateTime {
        val thisYearTarget = targetDate.withYear(now.year).atZone(zone)
        return if (now.isBefore(thisYearTarget)) {
            thisYearTarget
        } else {
            thisYearTarget.plusYears(1)
        }
    }

    fun isUnlocked(now: ZonedDateTime = ZonedDateTime.now(zone)): Boolean {
        return !now.isBefore(targetDate.atZone(zone))
    }

    fun countdownFlow(): Flow<Duration> = flow {
        while (true) {
            val now = ZonedDateTime.now(zone)
            val target = targetDate.atZone(zone)
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

    fun nextBirthdayCountdownFlow(): Flow<Duration> = flow {
        while (true) {
            val now = ZonedDateTime.now(zone)
            val target = nextTargetDate(now)
            val duration = Duration.between(now, target)
            emit(duration)
            delay(1000)
        }
    }
}
