package com.erdodif.capsulate.utility

import com.erdodif.capsulate.KParcelable
import com.erdodif.capsulate.KParcelize
import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.Serializable

object ChannelRepository {
    private var ID: Int = 0
    private val channels: MutableMap<Int, Channel<*>> = mutableMapOf()

    @Suppress("UNCHECKED_CAST")
    private operator fun <T>get(id: Int): Channel<T>{
        if(channels[id] == null){
            channels[id] = Channel<T>()
        }
        return channels[id] as Channel<T>
    }

    @KParcelize
    @Serializable
    data class ChannelEntry<T>(private val id: Int) : Channel<T> by get<T>(id), KParcelable

    fun <T>getNewChannel(): ChannelEntry<T> = ChannelEntry(ID++)
}
