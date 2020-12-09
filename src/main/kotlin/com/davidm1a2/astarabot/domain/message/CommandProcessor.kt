package com.davidm1a2.astarabot.domain.message

import com.davidm1a2.astarabot.domain.message.data.MessagePlayer
import com.davidm1a2.astarabot.domain.message.processor.MessageDispatcher
import com.davidm1a2.astarabot.persistent.Listings
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.client.resources.I18n
import kotlin.math.max

class CommandProcessor(private val sender: MessageDispatcher, listings: Listings) {
    private val dispatcher: CommandDispatcher<MessagePlayer> = CommandDispatcher()

    init {
        // help -> prints out bot help
        dispatcher.register(
            literal<MessagePlayer>("help")
                .executes {
                    sender.send(it.source, "AstaraBot Commands:")
                    sender.send(it.source, "listing remove <item=ALL>")
                    sender.send(it.source, "listing by <player=${it.source.name}>")
                    sender.send(it.source, "listing find <item>")
                    sender.send(it.source, "sell <item> <count> <price in diamonds>")
                    sender.send(it.source, "buy <item> <player> <multiplier=1>")
                    1
                }
        )

        // listings -> prints out your listings
        dispatcher.register(
            literal<MessagePlayer>("listing")
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
        //if (Minecraft.getInstance().player?.gameProfile?.id != player.id) {
        try {
            dispatcher.execute(command, player)
        } catch (e: CommandSyntaxException) {
            sender.send(player, e.rawMessage.string)

            // Copied from  Vanilla
            if (e.input != null && e.cursor >= 0) {
                val errorPos = e.input.length.coerceAtMost(e.cursor)
                var errorStr = ""
                if (errorPos > 10) {
                    errorStr = "..."
                }
                errorStr += e.input.substring(max(errorPos - 10, 0), errorPos)
                errorStr += I18n.format("command.context.here")
                sender.send(player, errorStr)
            }
        }
        //}
    }
}