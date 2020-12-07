package com.davidm1a2.astarabot.domain

import com.davidm1a2.astarabot.domain.message.processor.MessageHandler
import com.davidm1a2.astarabot.domain.message.processor.MessageParser
import net.minecraft.util.text.ChatType
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

class IngameChatHandler(
    private val handler: MessageHandler,
    private val parser: MessageParser
) {
    @SubscribeEvent
    fun onClientChatReceivedEvent(event: ClientChatReceivedEvent) {
        if (event.type == ChatType.SYSTEM) { // System message = sent by the server not by the client
            val message = parser.parse(event.message.string)
            println("Received: $message")
            handler.receive(message)
        }
    }
}