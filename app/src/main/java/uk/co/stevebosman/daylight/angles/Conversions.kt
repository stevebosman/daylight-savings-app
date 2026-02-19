package uk.co.stevebosman.daylight.angles

import kotlin.math.PI


/**
 * Convert a [value] in radians to the equivalent value in degrees.
 */
fun radiansToDegrees(value: Number): Double {
    return (180.0 * value.toDouble() / PI)
}

/**
 * Convert a [value] in degrees to the equivalent value in radians.
 */
fun degreesToRadians(value: Number): Double {
    return (PI * value.toDouble() / 180.0)
}


