package com.davidm1a2.astarabot.domain.message.processor

import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max

class SenderThrottler {
    private val throttleValues = ConcurrentHashMap<UUID, Int>()

    fun isThrottled(playerId: UUID): Boolean {
        return throttleValues[playerId] ?: 0 > THROTTLE_THRESHOLD
    }

    fun record(playerId: UUID) {
        val value = throttleValues[playerId] ?: 0
        throttleValues[playerId] = value + THROTTLE_PER_COMMAND
    }

    @SubscribeEvent
    fun onClientTickEvent(event: TickEvent.ClientTickEvent) {
        if (event.phase == TickEvent.Phase.START) {
            for (playerId in throttleValues.keys) {
                throttleValues[playerId] = max(0, throttleValues[playerId]!! - 1)
            }
        }
    }

    companion object {
        private const val THROTTLE_THRESHOLD = 100
        private const val THROTTLE_PER_COMMAND = 50
    }
}