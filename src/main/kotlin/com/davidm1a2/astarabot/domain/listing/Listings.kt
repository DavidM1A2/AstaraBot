package com.davidm1a2.astarabot.domain.listing

import com.davidm1a2.astarabot.domain.IdPlayer
import net.minecraft.item.Item
import net.minecraft.util.IItemProvider
import java.util.*

class Listings {
    private val playerListings: MutableMap<UUID, MutableSet<Listing>> = mutableMapOf()
    private val itemListings: MutableMap<Item, MutableSet<Listing>> = mutableMapOf()

    fun loadFrom(other: Listings) {
        this.playerListings.clear()
        this.itemListings.clear()
        this.playerListings.putAll(other.playerListings)
        this.itemListings.putAll(other.itemListings)
    }

    fun add(listing: Listing): Boolean {
        val playerToListings = playerListings.computeIfAbsent(listing.seller.id) { mutableSetOf() }
        val itemListings = itemListings.computeIfAbsent(listing.item) { mutableSetOf() }
        // Each seller can only sell an item once
        val wasReplaced = remove(listing.seller, listing.item)
        playerToListings.add(listing)
        itemListings.add(listing)
        return wasReplaced
    }

    fun get(player: IdPlayer): MutableSet<Listing>? {
        return playerListings[player.id]
    }

    fun get(item: IItemProvider): MutableSet<Listing>? {
        return itemListings[item.asItem()]
    }

    fun get(player: IdPlayer, item: IItemProvider): Listing? {
        return playerListings[player.id]?.firstOrNull { it.item == item.asItem() }
    }

    fun remove(player: IdPlayer, item: IItemProvider): Boolean {
        val listing = get(player, item)
        return if (listing != null) {
            playerListings[listing.seller.id]?.remove(listing)
            itemListings[listing.item]?.remove(listing)
            true
        } else {
            false
        }
    }

    fun removeAll(player: IdPlayer) {
        val output = playerListings.remove(player.id)
        output?.forEach {
            itemListings[it.item]?.remove(it)
        }
    }

    fun count(player: IdPlayer): Int {
        return playerListings[player.id]?.size ?: 0
    }
}