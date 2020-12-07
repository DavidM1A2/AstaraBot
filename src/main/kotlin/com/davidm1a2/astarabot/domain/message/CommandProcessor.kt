package com.davidm1a2.astarabot.domain.message

import com.davidm1a2.astarabot.domain.message.data.MessagePlayer
import com.davidm1a2.astarabot.domain.message.processor.MessageDispatcher
import com.davidm1a2.astarabot.persistent.Listings
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.client.resources.I18n

class CommandProcessor(private val sender: MessageDispatcher, listings: Listings) {
    private val dispatcher: CommandDispatcher<MessagePlayer> = CommandDispatcher()

    init {
        // help -> prints out bot help
        dispatcher.register(
            literal<MessagePlayer>("help")
                .executes {
                    sender.send(it.source, "AstaraBot Commands:")
                    sender.send(it.source, "listings")
                    1
                }
        )

        // listings -> prints out your listings
        dispatcher.register(
            literal<MessagePlayer>("listings")
                .executes {
                    val listingSet = listings.list(it.source.id)
                    if (listingSet.isEmpty()) {
                        sender.send(it.source, "You have no item listings")
                    } else {
                        listingSet.sortedBy { entry -> entry.item.name.formattedText }.forEach { entry ->
                            sender.send(
                                it.source,
                                "Selling ${entry.item.name.formattedText} for ${entry.price} diamonds"
                            )
                        }
                    }
                    1
                }
        )
    }

    fun process(player: MessagePlayer, command: String) {
        try {
            dispatcher.execute(command, player)
        } catch (e: CommandSyntaxException) {
            sender.send(player, e.rawMessage.string)

            if (e.input != null && e.cursor >= 0) {
                val errorPos = e.input.length.coerceAtMost(e.cursor)
                var errorStr = ""
                if (errorPos > 10) {
                    errorStr = "$errorStr..."
                }
                errorStr += e.input.substring(0.coerceAtLeast(errorPos - 10), errorPos)
                errorStr += I18n.format("command.context.here")
                sender.send(player, errorStr)
            }
        }
    }
}