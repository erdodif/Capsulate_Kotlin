package com.erdodif.capsulate

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import com.slack.circuit.foundation.animation.AnimatedNavEvent
import com.slack.circuit.foundation.animation.AnimatedNavState
import com.slack.circuit.foundation.animation.AnimatedScreenTransform
import com.slack.circuit.runtime.ExperimentalCircuitApi

@OptIn(ExperimentalCircuitApi::class)
object DefaultScreenTransform: AnimatedScreenTransform{
    override fun AnimatedContentTransitionScope<AnimatedNavState>.exitTransition(
        animatedNavEvent: AnimatedNavEvent
    ): ExitTransition? = fadeOut(tween(550)) + shrinkOut(tween(350))

    override fun AnimatedContentTransitionScope<AnimatedNavState>.enterTransition(
        animatedNavEvent: AnimatedNavEvent
    ): EnterTransition? = null
}
