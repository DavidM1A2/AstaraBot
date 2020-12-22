package com.davidm1a2.astarabot.domain.listing

enum class PriceType(val suffix: String, val friendlyName: String) {
    DIAMONDS("d", "diamond(s)"),
    DIAMOND_BLOCKS("db", "diamond block(s)")
}