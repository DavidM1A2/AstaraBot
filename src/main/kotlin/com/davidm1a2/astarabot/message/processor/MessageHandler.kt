package com.davidm1a2.astarabot.message.processor

import com.davidm1a2.astarabot.message.data.Message

class MessageHandler {
    fun receive(message: Message) {
        println("Received: $message")
    }
}