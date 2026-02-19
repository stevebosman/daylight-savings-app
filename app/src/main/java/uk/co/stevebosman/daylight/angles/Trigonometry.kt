package uk.co.stevebosman.daylight.angles

import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

// Main functions

/**
 * Cosine
 */
fun cos(a: Angle): Double {
    return cos(a.radians)
}

/**
 * Sine
 */
fun sin(a: Angle): Double {
    return sin(a.radians)
}

/**
 * Tangent
 */
fun tan(a: Angle): Double {
    return tan(a.radians)
}

// Reciprocal functions

/**
 * Secant - same as `1/cos(a)`
 */
fun sec(a: Angle): Double {
    return 1 / cos(a.radians)
}

/**
 * Cosecant - same as `1/sin(a)`
 */
fun csc(a: Angle): Double {
    return 1 / sin(a.radians)
}

/**
 * Cotangent - same as `1/tan(a)`
 */
fun cot(a: Angle): Double {
    return 1 / tan(a.radians)
}

// Arc- (inverse) functions

/**
 * Arctangent - inverse of [tan].
 * [x] can be any real number,
 * and the result is an angle in the range `(-π/2,π/2)` radians.
 */
fun atan(x: Number): Angle {
    return Angle.fromRadians(atan(x.toDouble()))
}

/**
 * Arcsin - inverse of [sin].
 * [x] must be in the range `[-1,1]`,
 * and the result is an angle in the range `[-π/2,π/2]` radians.
 */
fun asin(x: Number): Angle {
    if (x.toDouble() > 1 || x.toDouble() < -1) {
        throw IllegalArgumentException("v should be between -1 and 1 (inclusive)")
    }
    return Angle.fromRadians(asin(x.toDouble()))
}

/**
 * Arccos - inverse of [cos].
 * [x] must be in the range `[-1,1]`,
 * and the result is an angle in the range `[0,π]` radians.
 */
fun acos(x: Number): Angle {
    if (x.toDouble() > 1 || x.toDouble() < -1) {
        throw IllegalArgumentException("v should be between -1 and 1 (inclusive)")
    }
    return Angle.fromRadians(acos(x.toDouble()))
}

/**
 * Arcsec - inverse of [sec].
 * [x] can be any real number excluding the range `[-1,1]`,
 * and the result is an angle in the range `[0,π]` (excluding `π/2`) radians.
 */
fun asec(x: Number): Angle {
    if (x.toDouble() > -1 && x.toDouble() < 1) {
        throw IllegalArgumentException("abs(v) should be greater than 1")
    }
    return Angle.fromRadians(acos(1 / x.toDouble()))
}

/**
 * Arccosec - inverse of [csc].
 * [x] can be any real number excluding the range `[-1,1]`,
 * and the result is an angle in the range `[-π/2,π/2]` (excluding `0`) radians.
 */
fun acsc(x: Number): Angle {
    if (x.toDouble() > -1 && x.toDouble() < 1) {
        throw IllegalArgumentException("abs(v) should be greater than 1")
    }
    return Angle.fromRadians(asin(1 / x.toDouble()))
}

/**
 * Arccotangent - inverse of [cot].
 * [x] can be any real number,
 * and the result is an angle in the range `(0,π)` radians.
 */
fun acot(x: Number): Angle {
    return Angle.fromRadians(atan(1 / x.toDouble()))
}

