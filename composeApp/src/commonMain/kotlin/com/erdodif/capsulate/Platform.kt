package com.erdodif.capsulate

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform