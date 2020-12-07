package com.davidm1a2.astarabot

import com.davidm1a2.astarabot.domain.Constants
import com.davidm1a2.astarabot.domain.IngameChatHandler
import com.davidm1a2.astarabot.domain.message.CommandProcessor
import com.davidm1a2.astarabot.domain.message.processor.MessageDispatcher
import com.davidm1a2.astarabot.domain.message.processor.MessageHandler
import com.davidm1a2.astarabot.domain.message.processor.MessageParser
import com.davidm1a2.astarabot.persistent.Listings
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.LogicalSide
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.thread.EffectiveSide

@Mod(Constants.MOD_ID)
class AstaraBot {
    init {
        val forgeBus = MinecraftForge.EVENT_BUS

        val listings = Listings()

        val commandProcessor = CommandProcessor(MessageDispatcher(), listings)
        val messageHandler = MessageHandler(commandProcessor)
        if (EffectiveSide.get() == LogicalSide.CLIENT) {
            forgeBus.register(IngameChatHandler(messageHandler, MessageParser()))
        }
    }
}