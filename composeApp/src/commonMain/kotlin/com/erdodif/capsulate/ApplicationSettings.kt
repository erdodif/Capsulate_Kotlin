package com.erdodif.capsulate

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set

enum class AnimationSpeed(val baselineMs: Int){
    SLOWEST(1500),
    SLOW(600),
    MEDIUM(500),
    FAST(350),
    FASTEST(200),
    NEAR_INSTANT(100),
    INSTANT(0)
}

private

object ApplicationSettings{
    private val settings: Settings = Settings()
    val animationSpeed: AnimationSpeed = with(settings.get<AnimationSpeed>("animation-speed")) {
        if (this == null){
            settings["animation-speed"] = AnimationSpeed.MEDIUM
            AnimationSpeed.MEDIUM
        }
        else{
            this
        }
    }
}