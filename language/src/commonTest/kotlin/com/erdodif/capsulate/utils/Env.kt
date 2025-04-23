package com.erdodif.capsulate.utils

import com.erdodif.capsulate.lang.program.evaluation.Env

val EMPTY_ENVIRONMENT: Env
    get() = Env(mapOf(), mapOf(), mutableListOf(), 0)
