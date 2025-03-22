package com.erdodif.capsulate

import com.erdodif.capsulate.utility.AnimationSpeed
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set

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
