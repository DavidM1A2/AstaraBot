package com.davidm1a2.astarabot.message.data

data class Message(
    val type: MessageType,
    val body: String,
    val sender: MessageAccount = MessageAccount.UNKNOWN
)