package com.davidm1a2.astarabot.domain.message.processor

import com.davidm1a2.astarabot.domain.message.CommandProcessor
import com.davidm1a2.astarabot.domain.message.data.Message
import com.davidm1a2.astarabot.domain.message.data.MessageType

class MessageHandler(private val senderThrottler: SenderThrottler, private val commandProcessor: CommandProcessor) {
    fun receive(message: Message) {
        when (message.type) {
            MessageType.PM -> {
                if (!senderThrottler.isThrottled(message.sender.id)) {
                    senderThrottler.record(message.sender.id)
                    commandProcessor.process(message.sender, message.body)
                } else {
                    println("Throttled message from: ${message.sender.name}")
                }
            }
            MessageType.LOGIN -> println("Player ${message.sender.name} logged in!")
            MessageType.LOGOUT -> println("Player ${message.sender.name} logged out!")
            else -> Unit
        }
    }
}