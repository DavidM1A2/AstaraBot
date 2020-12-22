package com.davidm1a2.astarabot.domain.command

import com.davidm1a2.astarabot.domain.IdPlayer
import com.davidm1a2.astarabot.domain.message.MessageDispatcher
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType.*
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import net.minecraft.client.Minecraft

class MsgCommand(private val sender: MessageDispatcher) : BotCommand {
    override fun register(dispatcher: CommandDispatcher<IdPlayer>) {
        val msgTail = argument<IdPlayer, String>("player", word())
            .then(argument<IdPlayer, String>("message", greedyString())
                .executes {
                    val playerName = getString(it, "player")
                    val message = getString(it, "message")
                    sendMessageTo(it.source, playerName, message)
                    1
                })

        dispatcher.register(literal<IdPlayer>("msg").then(msgTail))
        dispatcher.register(literal<IdPlayer>("m").then(msgTail))
    }

    private fun sendMessageTo(player: IdPlayer, targetPlayerName: String, message: String) {
        if (Minecraft.getInstance().player?.gameProfile?.name?.equals(targetPlayerName, ignoreCase = true) == true) {
            sender.send(player, "You can't message me...")
        } else {
            val targetPlayer = Minecraft.getInstance().connection?.getPlayerInfo(targetPlayerName)?.gameProfile
            if (targetPlayer == null) {
                sender.send(player, "$targetPlayerName is offline")
            } else {
                sender.send(IdPlayer(targetPlayer.name, targetPlayer.id), "<Anonymous> $message")
            }
        }
    }
}