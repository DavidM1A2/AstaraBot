package com.davidm1a2.astarabot.domain.command

import com.davidm1a2.astarabot.domain.IdPlayer
import com.davidm1a2.astarabot.domain.message.MessageDispatcher
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.client.resources.I18n
import kotlin.math.max

class CommandProcessor(private val sender: MessageDispatcher, private val blacklist: Set<String>, commands: List<BotCommand>) {
    private val dispatcher: CommandDispatcher<IdPlayer> = CommandDispatcher()

    init {
        commands.forEach { it.register(dispatcher) }
    }

    fun process(player: IdPlayer, command: String) {
        if (!isBlacklisted(player)) {
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
        }
    }

    private fun isBlacklisted(player: IdPlayer): Boolean {
        return blacklist.contains(player.name)
    }
}