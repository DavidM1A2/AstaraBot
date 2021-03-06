package com.davidm1a2.astarabot

import com.davidm1a2.astarabot.dataaccess.DataStorer
import com.davidm1a2.astarabot.domain.Constants
import com.davidm1a2.astarabot.domain.IngameChatHandler
import com.davidm1a2.astarabot.domain.command.CommandProcessor
import com.davidm1a2.astarabot.domain.command.HelpCommand
import com.davidm1a2.astarabot.domain.command.ListingCommand
import com.davidm1a2.astarabot.domain.command.MsgCommand
import com.davidm1a2.astarabot.domain.listing.ListingHelper
import com.davidm1a2.astarabot.domain.listing.Listings
import com.davidm1a2.astarabot.domain.message.MessageDispatcher
import com.davidm1a2.astarabot.domain.message.MessageHandler
import com.davidm1a2.astarabot.domain.message.MessageParser
import com.davidm1a2.astarabot.domain.message.SenderThrottler
import com.davidm1a2.astarabot.domain.packet.PacketInterceptor
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.LogicalSide
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.thread.EffectiveSide

@Mod(Constants.MOD_ID)
class AstaraBot {
    init {
        val forgeBus = MinecraftForge.EVENT_BUS

        val listings = Listings()
        val listingHelper = ListingHelper(listings)

        val messageDispatcher = MessageDispatcher()

        val commands = listOf(
            HelpCommand(messageDispatcher),
            ListingCommand(messageDispatcher, listingHelper),
            MsgCommand(messageDispatcher)
        )

        val commandProcessor = CommandProcessor(messageDispatcher, setOf("Robot_Francis", "David_M1A2"), commands)
        val senderThrottler = SenderThrottler()
        val messageHandler = MessageHandler(senderThrottler, commandProcessor)

        if (EffectiveSide.get() == LogicalSide.CLIENT) {
            forgeBus.register(IngameChatHandler(messageHandler, MessageParser()))
            forgeBus.register(PacketInterceptor())
            forgeBus.register(messageDispatcher)
            forgeBus.register(senderThrottler)
            forgeBus.register(DataStorer(listings))
        }
    }
}