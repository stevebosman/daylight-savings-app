package uk.co.stevebosman.daylight.difference.scaling

import kotlin.math.*

/**
 * Logarithmic change
 */
fun logarithmicChangeFunction(a: Double, b: Double): Double {
    return if (a == b) {
        a
    } else {
        (b - a) / ln(b / a)
    }
}

/**
 * Harmonic mean
 */
fun harmonicMean(a: Double, b: Double): Double {
    return (2 * a * b) / (a + b)
}

/**
 * Geometric mean: Sqrt([a]*[b])
 */
fun geometricMean(a: Double, b: Double): Double {
    return sqrt(a * b)
}

/**
 * Maximum of two numbers: Max([a],[b])
 */
fun maxAOrB(a: Double, b: Double): Double {
    return max(a, b)
}

/**
 * Maximum in absolute size of two numbers: Max(|[a]|,|[b]|)
 */
fun maxAbsAOrB(a: Double, b: Double): Double {
    return max(abs(a), abs(b))
}

/**
 * Minimum in absolute size of two numbers: Min(|[a]|,|[b]|)
 */
fun minAbsAOrB(a: Double, b: Double): Double {
    return min(abs(a), abs(b))
}

/**
 * Minimum of two numbers: Max([a],[b])
 */
fun minAOrB(a: Double, b: Double): Double {
    return min(a, b)
}

/**
 * Mean of the absolutes of two numbers: Mean: (|[a]|+|[b]|)/2, using Welford's method
 */
fun meanAbs(a: Double, b: Double): Double {
    val absA = abs(a)
    val absB = abs(b)
    return mean(absA, absB)
}

/**
 * Mean of two numbers: Mean: ([a]+[b])/2, using Welford's method
 */
fun mean(a: Double, b: Double): Double {
    return a + (b - a) / 2.0
}

/**
 * Absolute of first number: abs([a])
 */
fun absA(a: Double, @Suppress("UNUSED_PARAMETER") b: Double): Double {
    return abs(a)
}

/**
 * Absolute of second number: abs([b])
 */
fun absB(@Suppress("UNUSED_PARAMETER") a: Double, b: Double): Double {
    return abs(b)
}


