package com.davidm1a2.astarabot.domain.message.processor

import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import java.util.*
import kotlin.math.max

class SenderThrottler {
    private val throttleValues = mutableMapOf<UUID, Int>()

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
            throttleValues.mapValues { max(0, it.value - 1) }
        }
    }

    companion object {
        private const val THROTTLE_THRESHOLD = 100
        private const val THROTTLE_PER_COMMAND = 50
    }
}