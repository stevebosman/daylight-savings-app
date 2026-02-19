package uk.co.stevebosman.daylight.difference

import uk.co.stevebosman.daylight.difference.scaling.maxAbsAOrB
import java.lang.Double.isNaN
import kotlin.math.abs

/**
 * Check if [a] is approximately equal to [b],
 * that is if:
 *   abs([a] - [b]) <= max([relativeTolerance] * [scalingFunction] ([a],[b]), [absoluteTolerance])
 */
fun isClose(
    a: Double, b: Double,
    relativeTolerance: Double = 1e-09,
    absoluteTolerance: Double = 0.0,
    equalNaN: Boolean = false,
    scalingFunction: (Double, Double) -> Double = ::maxAbsAOrB
): Boolean {
    var result = false
    if (equalNaN && isNaN(a) && isNaN(b)) {
        // NaNs can, optionally, be close to other NaNs
        result = true
    } else if (a == b) {
        // Same values are always close
        result = true
    } else if (a == Double.POSITIVE_INFINITY || a == Double.NEGATIVE_INFINITY || b == Double.POSITIVE_INFINITY || b == Double.NEGATIVE_INFINITY) {
        // Infinities are never close to other values except themselves
        result = false
    } else {
        val absoluteDifference = absoluteDifference(a, b)
        if (absoluteTolerance >= absoluteDifference) {
            result = true
        } else {
            val relativeDifference = absoluteDifference / scalingFunction(a, b)
            if (relativeTolerance >= relativeDifference) {
                result = true
            }
        }
    }

    return result
}

/**
 * Determine absolute difference between two values [a] and [b],
 * i.e. |[a]-[b]|
 */
fun absoluteDifference(
    a: Double, b: Double
) = abs(a - b)

