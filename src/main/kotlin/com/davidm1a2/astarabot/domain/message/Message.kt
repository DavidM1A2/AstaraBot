package com.davidm1a2.astarabot.domain.message

import com.davidm1a2.astarabot.domain.IdPlayer

data class Message(
    val type: MessageType,
    val body: String,
    val sender: IdPlayer = IdPlayer.UNKNOWN
)