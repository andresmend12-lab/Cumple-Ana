package com.example.birthday.data.model

import java.time.ZoneId
import java.time.ZonedDateTime

private val MadridZone: ZoneId = ZoneId.of("Europe/Madrid")

/**
 * Static schedule for the birthday activities.
 */
object ActivitySeeds {
    val zoneId: ZoneId = MadridZone

    data class Seed(
        val id: Int,
        val title: String,
        val description: String,
        val order: Int,
        val unlockAt: ZonedDateTime
    ) {
        val unlockAtEpochMillis: Long = unlockAt.toInstant().toEpochMilli()
    }

    val activities: List<Seed> = listOf(
        Seed(
            id = 1,
            order = 1,
            title = "Coronación",
            description = "El día comienza como debe: como una reina.\nPonte la corona, sopla las velas y que empiece el reinado del caos.",
            unlockAt = ZonedDateTime.of(2025, 12, 3, 0, 0, 0, 0, zoneId)
        ),
        Seed(
            id = 2,
            order = 2,
            title = "Desayuno",
            description = "Desayuno en la cama con bacon, café y muchos besos",
            unlockAt = ZonedDateTime.of(2025, 12, 3, 9, 0, 0, 0, zoneId)
        ),
        Seed(
            id = 3,
            order = 3,
            title = "Cuídate",
            description = "Hoy no toca correr, ni pensar, ni preocuparse.\nToca cuidarse, mimarse y dejar que el estrés se vaya por donde vino.",
            unlockAt = ZonedDateTime.of(2025, 12, 3, 10, 30, 0, 0, zoneId)
        ),
        Seed(
            id = 4,
            order = 4,
            title = "Que rico el cafesito",
            description = "Vamos por un café de los buenos,\naunque ninguno va a saber como los de casa.",
            unlockAt = ZonedDateTime.of(2025, 12, 3, 12, 0, 0, 0, zoneId)
        ),
        Seed(
            id = 5,
            order = 5,
            title = "Una nunca es suficiente",
            description = "Ya sabes cómo va esto: un cumpleaños sin exceso de tarta no es un cumpleaños.\nRepetimos tradición.",
            unlockAt = ZonedDateTime.of(2025, 12, 3, 15, 0, 0, 0, zoneId)
        ),
        Seed(
            id = 6,
            order = 6,
            title = "Siempre divina",
            description = "Siempre divina. A veces por perfume, siempre por naturaleza.",
            unlockAt = ZonedDateTime.of(2025, 12, 3, 16, 30, 0, 0, zoneId)
        ),
        Seed(
            id = 7,
            order = 7,
            title = "El dorado te queda bien",
            description = "Hay colores que no elige uno: el dorado te eligió a ti.",
            unlockAt = ZonedDateTime.of(2025, 12, 3, 18, 0, 0, 0, zoneId)
        ),
        Seed(
            id = 8,
            order = 8,
            title = "Acabar de la mejor forma",
            description = "Si todo lo bueno tiene un final,\nque el tuyo sea con sushi.",
            unlockAt = ZonedDateTime.of(2025, 12, 3, 19, 0, 0, 0, zoneId)
        )
    )
}
