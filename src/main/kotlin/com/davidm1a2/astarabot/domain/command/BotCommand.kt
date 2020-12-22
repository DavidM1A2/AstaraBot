package com.davidm1a2.astarabot.domain.command

import com.davidm1a2.astarabot.domain.IdPlayer
import com.mojang.brigadier.CommandDispatcher

interface BotCommand {
    fun register(dispatcher: CommandDispatcher<IdPlayer>)
}