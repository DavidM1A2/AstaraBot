package com.davidm1a2.astarabot.domain.message.processor

import com.davidm1a2.astarabot.domain.message.CommandProcessor
import com.davidm1a2.astarabot.domain.message.data.Message
import com.davidm1a2.astarabot.domain.message.data.MessageType

class MessageHandler(private val commandProcessor: CommandProcessor) {
    fun receive(message: Message) {
        when (message.type) {
            MessageType.PM -> commandProcessor.process(message.sender, message.body)
            MessageType.LOGIN -> println("Player ${message.sender.name} logged in!")
            MessageType.LOGOUT -> println("Player ${message.sender.name} logged out!")
        }
    }
}