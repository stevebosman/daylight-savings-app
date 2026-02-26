package uk.co.stevebosman.daylight.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.co.stevebosman.daylight.angles.Angle
import uk.co.stevebosman.daylight.moon.MoonPhase
import uk.co.stevebosman.daylight.sleep.sleepCalculation
import uk.co.stevebosman.daylight.sleep.wakeCalculation
import uk.co.stevebosman.daylight.sunrise.DaylightType
import uk.co.stevebosman.daylight.sunrise.calculateSunriseDetails
import uk.co.stevebosman.daylight.ui.theme.MainActivityTheme
import java.time.ZonedDateTime

@Composable
fun DaylightTimesScreen(
    latitude: Double,
    longitude: Double,
    locationName: String,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Location(locationName, latitude, longitude)
        DatesColumn(latitude, longitude)
    }
}

@Composable
fun Location(name: String, latitude: Double, longitude: Double, modifier: Modifier = Modifier) {
    FlowRow {
        Text(text = name, modifier)
        Text(text = "(${formatLatitude(latitude)} ${formatLongitude(longitude)})", modifier)
    }
}

@Composable
private fun DatesColumn(
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(count = 365) { i ->
            Date(
                offset = i.toLong(),
                latitude = latitude,
                longitude = longitude
            )
        }
    }
}

@Composable
fun Date(
    offset: Long,
    latitude: Number,
    longitude: Number,
    modifier: Modifier = Modifier
) {
    val date = ZonedDateTime.now().withHour(12).plusDays(offset)
    val yesterday =
        calculateSunriseDetails(
            date.minusDays(1L),
            Angle.fromDegrees(longitude),
            Angle.fromDegrees(latitude)
        )
    val currentDay =
        calculateSunriseDetails(date, Angle.fromDegrees(longitude), Angle.fromDegrees(latitude))
    val tomorrow =
        calculateSunriseDetails(
            date.plusDays(1L),
            Angle.fromDegrees(longitude),
            Angle.fromDegrees(latitude)
        )

    val sunrise =
        when (currentDay.sunriseType) {
            DaylightType.MIDNIGHT_SUN -> "\u2600\ufe0f" + formatShortDate(currentDay.sunriseTime)
            DaylightType.POLAR_NIGHT -> "\u2b1b" + formatShortDate(currentDay.sunsetTime)
            else -> "\uD83C\uDF05" + formatTime(currentDay.sunriseTime)
        }
    val sunset =
        MoonPhase.of(currentDay.moonPhase).icon + when (currentDay.sunsetType) {
            DaylightType.MIDNIGHT_SUN -> formatShortDate(currentDay.sunsetTime)
            DaylightType.POLAR_NIGHT -> formatShortDate(currentDay.sunriseTime)
            else -> formatTime(currentDay.sunsetTime)
        }

    val wake = formatTime(
        wakeCalculation(
            yesterday.sunsetTime,
            currentDay.sunriseTime, currentDay.sunriseType
        )
    )
    val sleepTime =
        sleepCalculation(currentDay.sunsetTime, currentDay.sunsetType, tomorrow.sunriseTime)
    val sleep = formatTime(sleepTime)

    Row(
        modifier
            .padding(0.dp, 4.dp)
            .fillMaxWidth()
    ) {
        Column(modifier.weight(0.22f)) {
            Text(
                text = sunrise,
                softWrap = false,
                modifier = modifier
            )
            Text(
                text = "\u23f0" + wake,
                softWrap = false,
                modifier = modifier
            )
        }
        Text(
            text = formatLongDate(currentDay.solarNoonTime),
            minLines = 2,
            textAlign = TextAlign.Center,
            modifier = modifier
                .padding(horizontal = 16.dp)
                .weight(0.56f)
        )
        Column(modifier.weight(0.22f)) {
            Text(
                text = sunset,
                softWrap = false,
                modifier = modifier
            )
            Text(
                text = "\uD83D\uDCA4" + sleep,
                softWrap = false,
                modifier = modifier
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DaylightTimesScreenPreview() {
    MainActivityTheme {
        DaylightTimesScreen(
            52.61,
            -1.92,
            "Aldridge, Walsall, West Midlands",
            Modifier.border(BorderStroke(1.dp, Color.Red))
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    MainActivityTheme {
        Date(
            0,
            52.61,
            -1.92,
            Modifier.border(BorderStroke(1.dp, Color.Red))
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ArcticPreview() {
    MainActivityTheme {
        Date(0, 85, -1.92)
    }
}

@Preview(showBackground = true)
@Composable
fun AntarcticPreview() {
    MainActivityTheme {
        Date(0, -85, -1.92)
    }
}
