package com.davidm1a2.astarabot.domain.command

import com.davidm1a2.astarabot.domain.message.data.IdPlayer
import com.davidm1a2.astarabot.domain.message.processor.MessageDispatcher
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder

class HelpCommand(private val sender: MessageDispatcher) : BotCommand {
    override fun register(dispatcher: CommandDispatcher<IdPlayer>) {
        dispatcher.register(
            // help -> prints out bot help
            LiteralArgumentBuilder.literal<IdPlayer>("help")
                .executes {
                    sender.send(it.source, "listing mine")
                    sender.send(it.source, "listing find <item> <includeOffline=true>")
                    sender.send(it.source, "listing remove <item=ALL>")
                    sender.send(it.source, "sell <item> <count> <price in diamonds>")
                    sender.send(it.source, "buy <item> <player> <multiplier=1>")
                    sender.send(it.source, "msg <player> <message>")
                    1
                }
        )
    }
}