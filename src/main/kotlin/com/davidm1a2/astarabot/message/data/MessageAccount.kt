package com.davidm1a2.astarabot.message.data

import java.util.*

data class MessageAccount(
    private val name: String,
    private val id: UUID
) {
    companion object {
        val UNKNOWN = MessageAccount("", UUID.fromString("00000000-0000-0000-0000-000000000000"))
    }
}
