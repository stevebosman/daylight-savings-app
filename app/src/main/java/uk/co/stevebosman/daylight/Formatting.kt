package uk.co.stevebosman.daylight

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue

fun formatLongDate(date: ZonedDateTime): String = date.toLocalDate().format(
    DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
)

fun formatShortDate(date: ZonedDateTime): String = date.toLocalDate().format(
    DateTimeFormatter.ofPattern("dd-MMM")
)

fun formatTime(date: ZonedDateTime): String =
    date.toLocalTime().plusSeconds(30).truncatedTo(ChronoUnit.MINUTES).format(
        DateTimeFormatter.ofPattern("HH:mm")
)

fun formatLatitude(latitude: Double): String {
    val direction = (if (latitude < 0) "S" else "N")
    return formatAngle(latitude) + direction
}

fun formatLongitude(longitude: Double): String {
    val direction = (if (longitude < 0) "W" else "E")
    return formatAngle(longitude) + direction
}

fun formatAngle(angle: Double): String {
    return "%,.3f".format(angle.absoluteValue)
}
