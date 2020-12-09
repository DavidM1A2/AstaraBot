package com.davidm1a2.astarabot.persistent

import net.minecraft.item.Item
import java.util.*

data class ListingEntry(
    val seller: UUID,
    val item: Item,
    val count: Int,
    val price: Int // in diamonds
)