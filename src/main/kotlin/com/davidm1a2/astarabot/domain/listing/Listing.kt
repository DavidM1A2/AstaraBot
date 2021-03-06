package com.davidm1a2.astarabot.domain.listing

import com.davidm1a2.astarabot.domain.IdPlayer
import net.minecraft.item.Item

data class Listing(
    val seller: IdPlayer,
    val item: Item,
    val count: Int,
    val price: Int,
    val priceType: PriceType
)