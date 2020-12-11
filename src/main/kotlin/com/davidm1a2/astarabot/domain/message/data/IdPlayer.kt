package com.davidm1a2.astarabot.domain.message.data

import java.util.*

data class IdPlayer(
    val name: String,
    val id: UUID
) {
    companion object {
        val UNKNOWN = IdPlayer("", UUID.fromString("00000000-0000-0000-0000-000000000000"))
    }
}
