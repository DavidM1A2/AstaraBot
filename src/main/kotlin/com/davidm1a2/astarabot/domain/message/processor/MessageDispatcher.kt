package com.davidm1a2.astarabot.domain.message.processor

import com.davidm1a2.astarabot.domain.message.data.MessagePlayer
import net.minecraft.client.Minecraft
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MessageDispatcher {
    private var currentSendingPlayer = MessagePlayer.UNKNOWN
    private val toSendQueue = linkedMapOf<MessagePlayer, MutableList<String>>()
    private val messageDelayer = Executors.newSingleThreadScheduledExecutor()

    fun send(player: MessagePlayer, message: String) {
        synchronized(toSendQueue) {
            if (currentSendingPlayer == player) {
                Minecraft.getInstance().player!!.sendChatMessage("/m $message")
            } else if (currentSendingPlayer == MessagePlayer.UNKNOWN) {
                currentSendingPlayer = player
                Minecraft.getInstance().player!!.sendChatMessage("/msg ${currentSendingPlayer.name} $message")
                messageDelayer.schedule(this::sendQueuedMessages, 2000, TimeUnit.MILLISECONDS)
            } else {
                val messages = toSendQueue.computeIfAbsent(player) { mutableListOf() }
                messages.add(message)
            }
        }
    }

    private fun sendQueuedMessages() {
        synchronized(toSendQueue) {
            if (toSendQueue.isNotEmpty()) {
                val iterator = toSendQueue.iterator()
                val nextEntry = iterator.next()
                iterator.remove()
                currentSendingPlayer = nextEntry.key

                val player = Minecraft.getInstance().player
                if (player != null) {
                    nextEntry.value.forEachIndexed { index, message ->
                        if (index == 0) {
                            player.sendChatMessage("/msg ${currentSendingPlayer.name} $message")
                        } else {
                            player.sendChatMessage("/m $message")
                        }
                    }
                }

                messageDelayer.schedule(this::sendQueuedMessages, 2000, TimeUnit.MILLISECONDS)
            } else {
                currentSendingPlayer = MessagePlayer.UNKNOWN
            }
        }
    }
}