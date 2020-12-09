package com.davidm1a2.astarabot.persistent

import com.davidm1a2.astarabot.domain.CommandResult
import net.minecraft.util.IItemProvider
import java.util.*

class Listings {
    private val playerListings: MutableMap<UUID, MutableSet<ListingEntry>> = mutableMapOf()

    fun add(sellerId: UUID, item: IItemProvider, count: Int, price: Int): CommandResult {
        val listings = playerListings.computeIfAbsent(sellerId) { mutableSetOf() }
        val isUpdating = listings.any { it.item == item.asItem() }
        return if (isUpdating || listings.size < MAX_LISTINGS) {
            val marketEntry = ListingEntry(sellerId, item.asItem(), count, price)
            if (isUpdating) {
                listings.removeIf { it.item == item.asItem() }
                listings.add(marketEntry)
                CommandResult("Successfully updated the price of ${item.asItem().name.formattedText} to $price diamonds")
            } else {
                listings.add(marketEntry)
                CommandResult("Successfully listed ${item.asItem().name.formattedText} for $price diamonds")
            }
        } else {
            CommandResult("Failed to add listing for ${item.asItem().name.formattedText}. Limit of $MAX_LISTINGS listings exceeded")
        }
    }

    fun remove(sellerId: UUID, item: IItemProvider): CommandResult {
        val removedSuccessfully = playerListings[sellerId]?.removeIf { it.item == item.asItem() } == true
        return if (removedSuccessfully) {
            CommandResult("Successfully removed listing for ${item.asItem().name.formattedText}s")
        } else {
            CommandResult("You don't have any ${item.asItem().name.formattedText} listed")
        }
    }

    fun removeAll(sellerId: UUID): CommandResult {
        playerListings.remove(sellerId)
        return CommandResult("Successfully removed all store listings")
    }

    fun list(sellerId: UUID): Set<ListingEntry> {
        return playerListings[sellerId] ?: emptySet()
    }

    companion object {
        private const val MAX_LISTINGS = 40
    }
}