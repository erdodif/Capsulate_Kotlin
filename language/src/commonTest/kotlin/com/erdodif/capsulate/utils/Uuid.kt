package com.erdodif.capsulate.utils

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
val Int.id: Uuid
    get() = Uuid.fromLongs(0L, toLong())
