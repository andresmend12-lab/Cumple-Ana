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
            description = "Coloca la corona y sopla las velas de la primera tarta (00:00).",
            unlockAt = ZonedDateTime.of(2025, 12, 3, 0, 0, 0, 0, zoneId)
        ),
        Seed(
            id = 2,
            order = 2,
            title = "Desayuno",
            description = "Desayuno en la cama con bacon, café y flores.",
            unlockAt = ZonedDateTime.of(2025, 12, 3, 9, 0, 0, 0, zoneId)
        ),
        Seed(
            id = 3,
            order = 3,
            title = "Cuídate",
            description = "Primer regalo: libro o detalle de calma/autocuidado.",
            unlockAt = ZonedDateTime.of(2025, 12, 3, 10, 30, 0, 0, zoneId)
        ),
        Seed(
            id = 4,
            order = 4,
            title = "Que rico el cafesito",
            description = "Paseo o momento relax con café en tu sitio favorito.",
            unlockAt = ZonedDateTime.of(2025, 12, 3, 12, 0, 0, 0, zoneId)
        ),
        Seed(
            id = 5,
            order = 5,
            title = "Una tarta nunca es suficiente",
            description = "Merienda con tarta 3 leches (segunda tarta).",
            unlockAt = ZonedDateTime.of(2025, 12, 3, 15, 0, 0, 0, zoneId)
        ),
        Seed(
            id = 6,
            order = 6,
            title = "Siempre divina",
            description = "Regalo secundario: perfume Divine Elixir.",
            unlockAt = ZonedDateTime.of(2025, 12, 3, 16, 30, 0, 0, zoneId)
        ),
        Seed(
            id = 7,
            order = 7,
            title = "El amarillo te queda bien",
            description = "Regalo principal: collar/colgante (dorado/amarillo).",
            unlockAt = ZonedDateTime.of(2025, 12, 3, 18, 0, 0, 0, zoneId)
        ),
        Seed(
            id = 8,
            order = 8,
            title = "Acabar de la mejor forma",
            description = "Cena omakase preparada con amor.",
            unlockAt = ZonedDateTime.of(2025, 12, 3, 19, 0, 0, 0, zoneId)
        )
    )
}
