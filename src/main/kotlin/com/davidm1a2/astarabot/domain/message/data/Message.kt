package com.davidm1a2.astarabot.domain.message.data

data class Message(
    val type: MessageType,
    val body: String,
    val sender: MessagePlayer = MessagePlayer.UNKNOWN
)