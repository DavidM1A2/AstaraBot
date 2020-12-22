package com.davidm1a2.astarabot.domain.command

import com.davidm1a2.astarabot.domain.IdPlayer
import com.davidm1a2.astarabot.domain.message.MessageDispatcher
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal

class HelpCommand(private val sender: MessageDispatcher) : BotCommand {
    override fun register(dispatcher: CommandDispatcher<IdPlayer>) {
        dispatcher.register(
            literal<IdPlayer>("help")
                .then(literal<IdPlayer>("listing").executes { printListingHelp(it.source) })
                .then(literal<IdPlayer>("l").executes { printListingHelp(it.source) })
                .then(literal<IdPlayer>("msg").executes { printMsgHelp(it.source) })
                .then(literal<IdPlayer>("m").executes { printMsgHelp(it.source) })
                .executes {
                    sender.send(it.source, "Use 'help <command>' to get details about a specific command: ")
                    sender.send(it.source, "listing (alias 'l') - Allows you to buy, sell, or search for items being sold")
                    sender.send(it.source, "msg (alias 'm') - Allows you to message players through the bot")
                    1
                }
        )
    }

    private fun printListingHelp(player: IdPlayer): Int {
        sender.send(player, "listing mine - Shows your current listings (aka what you're selling)")
        sender.send(player, "listing find <item> <includeOffline=true> - Searches for a seller of an item. If includeOffline is false only online player's listings are considered")
        sender.send(player, "listing remove <item=ALL> - Removes a listing of yours for a specific item. If the item is unspecified ALL listings are removed")
        sender.send(player, "listing sell <item> <count> <price><type=d/db> - Sell an item or items at a given price in diamonds or diamond blocks")
        sender.send(player, "listing buy <item> <player> <multiplier=1> - Request to buy an item from a player. Specify 'multiplier' to buy the listing in bulk")
        return 1
    }

    private fun printMsgHelp(player: IdPlayer): Int {
        sender.send(player, "msg <player> <message> - Anonymously send a message to a player")
        return 1
    }
}