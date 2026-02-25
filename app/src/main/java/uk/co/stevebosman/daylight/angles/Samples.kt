package uk.co.stevebosman.daylight.angles

import kotlin.math.PI

internal fun simplifyExample1(): Angle {
    // Simplifies to 180°
    return Angle.fromDegrees(540).simplify()
}

internal fun simplifyExample2(): Angle {
    // Simplifies to 90°
    return Angle.fromDegrees(450).simplify(-180, false)
}

internal fun simplifyExample3(): Angle {
    // Simplifies to -π radians
    return Angle.fromRadians(3 * PI).simplify(-PI, true)
}