package com.davidm1a2.astarabot.domain.message.processor

import com.davidm1a2.astarabot.domain.message.data.MessagePlayer
import com.davidm1a2.astarabot.domain.packet.ReceivePacketEvent
import net.minecraft.client.Minecraft
import net.minecraft.network.play.server.SUpdateTimePacket
import net.minecraftforge.client.event.ClientChatEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.max

class MessageDispatcher {
    private var lastServerWorldTime = 0L
    private var serverThrottleValue = AtomicLong(0)

    private var lastPlayerToSend = MessagePlayer.UNKNOWN
    private var processingQueue = false
    private val toSendQueue: Queue<PendingMessage> = LinkedList()
    private val messageDelayer = Executors.newSingleThreadScheduledExecutor()

    fun send(player: MessagePlayer, message: String) {
        synchronized(toSendQueue) {
            toSendQueue.add(PendingMessage(player, message))
            // If we're not currently processing the queue, begin processing the queue
            if (!processingQueue) {
                messageDelayer.execute(this::sendQueuedMessage)
                processingQueue = true
            }
        }
    }

    private fun sendQueuedMessage() {
        synchronized(toSendQueue) {
            if (toSendQueue.isNotEmpty()) {
                if (serverThrottleValue.get() < THROTTLE_MAX - THROTTLE_PER_MSG) {
                    val nextEntry = toSendQueue.remove()
                    if (nextEntry.player == lastPlayerToSend) {
                        Minecraft.getInstance().player!!.sendChatMessage("/m ${nextEntry.message}")
                        serverThrottleValue.addAndGet(THROTTLE_PER_MSG)
                        messageDelayer.execute(this::sendQueuedMessage)
                    } else {
                        lastPlayerToSend = nextEntry.player
                        Minecraft.getInstance().player!!.sendChatMessage("/msg ${lastPlayerToSend.name} ${nextEntry.message}")
                        serverThrottleValue.addAndGet(THROTTLE_PER_MSG)
                        messageDelayer.schedule(this::sendQueuedMessage, DELAY_SWITCHING_PLAYERS, TimeUnit.MILLISECONDS)
                    }

                    if (toSendQueue.isEmpty()) {
                        processingQueue = false
                    }
                } else {
                    messageDelayer.schedule(this::sendQueuedMessage, DELAY_WAITING_THROTTLE, TimeUnit.MILLISECONDS)
                }
            }
        }
    }

    @SubscribeEvent
    fun onReceivePacketEvent(event: ReceivePacketEvent) {
        if (event.packet is SUpdateTimePacket) {
            val timePassed = event.packet.totalWorldTime - lastServerWorldTime
            serverThrottleValue.set(max(0, serverThrottleValue.get() - timePassed))
            lastServerWorldTime = event.packet.totalWorldTime
        }
    }

    @SubscribeEvent
    fun onPlayerLogoutEvent(event: PlayerEvent.PlayerLoggedOutEvent) {
        synchronized(toSendQueue) {
            toSendQueue.clear()
            lastPlayerToSend = MessagePlayer.UNKNOWN
            serverThrottleValue.set(0)
            lastServerWorldTime = 0
            processingQueue = false
        }
    }

    @SubscribeEvent
    fun onPlayerSendMessageEvent(event: ClientChatEvent) {
        synchronized(toSendQueue) {
            serverThrottleValue.addAndGet(THROTTLE_PER_MSG)
        }
    }

    private data class PendingMessage(val player: MessagePlayer, val message: String)

    companion object {
        private const val DELAY_SWITCHING_PLAYERS = 2000L // 2 seconds
        private const val DELAY_WAITING_THROTTLE = 500L // 0.5 second

        private const val THROTTLE_PER_MSG = 20L
        private const val THROTTLE_MAX = 200L
    }
}