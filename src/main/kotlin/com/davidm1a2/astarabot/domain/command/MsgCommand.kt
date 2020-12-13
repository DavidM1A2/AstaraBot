package com.davidm1a2.astarabot.domain.command

import com.davidm1a2.astarabot.domain.message.data.IdPlayer
import com.davidm1a2.astarabot.domain.message.processor.MessageDispatcher
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType.*
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import net.minecraft.client.Minecraft

class MsgCommand(private val sender: MessageDispatcher) : BotCommand {
    override fun register(dispatcher: CommandDispatcher<IdPlayer>) {
        // msg <player> <message> - Messages a given player anonymously
        dispatcher.register(
            literal<IdPlayer>("msg")
                .then(
                    argument<IdPlayer, String>("player", word())
                        .then(argument<IdPlayer, String>("message", greedyString())
                            .executes {
                                val playerName = getString(it, "player")
                                val message = getString(it, "message")
                                if (Minecraft.getInstance().player?.gameProfile?.name?.equals(playerName, ignoreCase = true) == true) {
                                    sender.send(it.source, "You can't message me...")
                                } else {
                                    val targetPlayer = Minecraft.getInstance().connection?.getPlayerInfo(playerName)?.gameProfile
                                    if (targetPlayer == null) {
                                        sender.send(it.source, "$playerName is offline")
                                    } else {
                                        sender.send(IdPlayer(targetPlayer.name, targetPlayer.id), "<Anonymous> $message")
                                    }
                                }
                                1
                            })
                )
        )
    }
}