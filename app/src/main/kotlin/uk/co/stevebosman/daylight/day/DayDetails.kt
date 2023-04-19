package uk.co.stevebosman.daylight.day

import uk.co.stevebosman.sunrise.SunriseDetails
import java.time.ZonedDateTime

class DayDetails(val day: SunriseDetails,
                 val wakeUp: ZonedDateTime,
                 val sleep: ZonedDateTime)