package com.erdodif.capsulate.utility

enum class AnimationSpeed(val baselineMs: Int){
    SLOWEST(1500),
    SLOW(600),
    MEDIUM(500),
    FAST(350),
    FASTEST(200),
    NEAR_INSTANT(100),
    INSTANT(0)
}
