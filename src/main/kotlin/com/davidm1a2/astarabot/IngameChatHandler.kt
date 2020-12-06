package com.davidm1a2.astarabot

import com.davidm1a2.astarabot.message.processor.MessageHandler
import com.davidm1a2.astarabot.message.processor.MessageParser
import net.minecraft.util.text.ChatType
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

class IngameChatHandler(
    private val handler: MessageHandler,
    private val parser: MessageParser
) {
    @SubscribeEvent
    fun onClientChatReceivedEvent(event: ClientChatReceivedEvent) {
        if (event.type == ChatType.SYSTEM) {
            val message = parser.parse(event.message.string)
            handler.receive(message)
        }
    }
}