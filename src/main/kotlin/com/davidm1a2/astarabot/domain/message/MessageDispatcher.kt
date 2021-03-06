package com.davidm1a2.astarabot.domain.message

import com.davidm1a2.astarabot.domain.IdPlayer
import com.davidm1a2.astarabot.domain.packet.ReceivePacketEvent
import net.minecraft.client.Minecraft
import net.minecraft.network.play.server.SUpdateTimePacket
import net.minecraftforge.client.event.ClientChatEvent
import net.minecraftforge.client.event.ClientPlayerNetworkEvent
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.SubscribeEvent
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.math.max

class MessageDispatcher {
    private var lastServerWorldTime = 0L
    private var serverThrottleValue = 0L

    private var lastPlayerSentTo = IdPlayer.UNKNOWN
    private var threadStatus = ProcessStatus.WAITING
    private val msgQueue: Queue<PendingMessage> = LinkedList()
    private lateinit var messageDelayer: ScheduledExecutorService

    fun send(player: IdPlayer, message: String) {
        synchronized(msgQueue) {
            msgQueue.add(PendingMessage(player, message))
            // If we're not currently processing the queue, begin processing the queue
            if (threadStatus == ProcessStatus.WAITING) {
                threadStatus = ProcessStatus.PROCESSING
                messageDelayer.execute(this::sendQueuedMessage)
            }
        }
    }

    private fun sendQueuedMessage() {
        synchronized(msgQueue) {
            if (msgQueue.isNotEmpty()) {
                if (serverThrottleValue < THROTTLE_MAX - THROTTLE_PER_MSG) {
                    val pendingMessage = msgQueue.remove()
                    if (pendingMessage.player == lastPlayerSentTo) {
                        Minecraft.getInstance().player!!.sendChatMessage("/m ${pendingMessage.message}")
                        serverThrottleValue = serverThrottleValue + THROTTLE_PER_MSG
                        messageDelayer.execute(this::sendQueuedMessage)
                    } else {
                        lastPlayerSentTo = pendingMessage.player
                        Minecraft.getInstance().player!!.sendChatMessage("/msg ${lastPlayerSentTo.name} ${pendingMessage.message}")
                        serverThrottleValue = serverThrottleValue + THROTTLE_PER_MSG
                        messageDelayer.schedule(this::sendQueuedMessage, DELAY_SWITCHING_PLAYERS, TimeUnit.MILLISECONDS)
                    }

                    if (msgQueue.isEmpty()) {
                        threadStatus = ProcessStatus.WAITING
                    }
                } else {
                    threadStatus = ProcessStatus.THROTTLED
                }
            } else {
                threadStatus = ProcessStatus.WAITING
            }
        }
    }

    @SubscribeEvent
    fun onReceivePacketEvent(event: ReceivePacketEvent) {
        if (event.packet is SUpdateTimePacket) {
            synchronized(msgQueue) {
                val timePassed = event.packet.totalWorldTime - lastServerWorldTime
                serverThrottleValue = max(0, serverThrottleValue - timePassed)
                lastServerWorldTime = event.packet.totalWorldTime

                if (threadStatus == ProcessStatus.THROTTLED) {
                    threadStatus = ProcessStatus.PROCESSING
                    messageDelayer.execute(this::sendQueuedMessage)
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onPlayerLoginEvent(event: ClientPlayerNetworkEvent.LoggedInEvent) {
        synchronized(msgQueue) {
            messageDelayer = Executors.newSingleThreadScheduledExecutor()
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onPlayerLogoutEvent(event: ClientPlayerNetworkEvent.LoggedOutEvent) {
        synchronized(msgQueue) {
            if (::messageDelayer.isInitialized) {
                messageDelayer.shutdownNow()
            }
            msgQueue.clear()
            lastPlayerSentTo = IdPlayer.UNKNOWN
            serverThrottleValue = 0
            lastServerWorldTime = 0
            threadStatus = ProcessStatus.WAITING
        }
    }

    @SubscribeEvent
    fun onPlayerSendMessageEvent(event: ClientChatEvent) {
        synchronized(msgQueue) {
            serverThrottleValue = serverThrottleValue + THROTTLE_PER_MSG
        }
    }

    private data class PendingMessage(val player: IdPlayer, val message: String)

    private enum class ProcessStatus {
        WAITING,
        THROTTLED,
        PROCESSING
    }

    companion object {
        private const val DELAY_SWITCHING_PLAYERS = 2000L // 2 seconds

        private const val THROTTLE_PER_MSG = 20L
        private const val THROTTLE_MAX = 180L // Technically 200, but fix race conditions with some padding
    }
}