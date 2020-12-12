package com.davidm1a2.astarabot.persistent

import com.davidm1a2.astarabot.domain.message.data.IdPlayer
import net.minecraft.item.Item
import net.minecraft.util.IItemProvider

class Listings {
    private val playerListings: MutableMap<IdPlayer, MutableSet<Listing>> = mutableMapOf()
    private val itemListings: MutableMap<Item, MutableSet<Listing>> = mutableMapOf()

    fun add(listing: Listing): Boolean {
        val playerToListings = playerListings.computeIfAbsent(listing.seller) { mutableSetOf() }
        val itemListings = itemListings.computeIfAbsent(listing.item) { mutableSetOf() }
        // Each seller can only sell an item once
        val wasReplaced = remove(listing.seller, listing.item)
        playerToListings.add(listing)
        itemListings.add(listing)
        return wasReplaced
    }

    fun get(player: IdPlayer): MutableSet<Listing>? {
        return playerListings[player]
    }

    fun get(item: IItemProvider): MutableSet<Listing>? {
        return itemListings[item.asItem()]
    }

    fun get(player: IdPlayer, item: IItemProvider): Listing? {
        return playerListings[player]?.firstOrNull { it.item == item.asItem() }
    }

    fun remove(player: IdPlayer, item: IItemProvider): Boolean {
        val listing = get(player, item)
        return if (listing != null) {
            playerListings[listing.seller]?.remove(listing)
            itemListings[listing.item]?.remove(listing)
            true
        } else {
            false
        }
    }

    fun removeAll(player: IdPlayer) {
        val output = playerListings.remove(player)
        output?.forEach {
            itemListings[it.item]?.remove(it)
        }
    }

    fun count(player: IdPlayer): Int {
        return playerListings[player]?.size ?: 0
    }
}