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
            description = "El día empieza como debe: con una reina.\nPonte la corona, sopla las velas y que empiece el reinado del caos bonito. 👑🎂",
            unlockAt = ZonedDateTime.of(2025, 12, 3, 0, 0, 0, 0, zoneId)
        ),
        Seed(
            id = 2,
            order = 2,
            title = "Desayuno",
            description = "La realeza también desayuna... pero mejor.\nHoy el bacon cruje más, el café huele a calma y todo sabe a ti. ☕💛",
            unlockAt = ZonedDateTime.of(2025, 12, 3, 9, 0, 0, 0, zoneId)
        ),
        Seed(
            id = 3,
            order = 3,
            title = "Cuídate",
            description = "Entre tanto brillo, hay que parar un poco.\nEste regalo no se usa: se respira.\nTómate un momento solo tuyo. 🌿",
            unlockAt = ZonedDateTime.of(2025, 12, 3, 10, 30, 0, 0, zoneId)
        ),
        Seed(
            id = 4,
            order = 4,
            title = "Que rico el cafesito",
            description = "A veces el café no se bebe: se comparte.\nQue este momento te sepa a risas lentas y a cosas bonitas que no se apuran. ☕✨",
            unlockAt = ZonedDateTime.of(2025, 12, 3, 12, 0, 0, 0, zoneId)
        ),
        Seed(
            id = 5,
            order = 5,
            title = "Una tarta nunca es suficiente",
            description = "¿Una tarta? No, dos.\nPorque cuando se trata de dulzura, tú siempre te pasas de la medida. 🎂💞",
            unlockAt = ZonedDateTime.of(2025, 12, 3, 15, 0, 0, 0, zoneId)
        ),
        Seed(
            id = 6,
            order = 6,
            title = "Siempre divina",
            description = "Brillas aunque no quieras (y lo sabes).\nEste detalle no es para que huelas bien, sino para que recuerdes lo bien que te sienta ser tú. 💫",
            unlockAt = ZonedDateTime.of(2025, 12, 3, 16, 30, 0, 0, zoneId)
        ),
        Seed(
            id = 7,
            order = 7,
            title = "El amarillo te queda bien",
            description = "No todo lo que brilla es oro… pero esto casi.",
            unlockAt = ZonedDateTime.of(2025, 12, 3, 18, 0, 0, 0, zoneId)
        ),
        Seed(
            id = 8,
            order = 8,
            title = "Acabar de la mejor forma",
            description = "Si todo lo bueno tiene un final… que sea con sushi.",
            unlockAt = ZonedDateTime.of(2025, 12, 3, 19, 0, 0, 0, zoneId)
        )
    )
}
