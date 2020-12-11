package com.davidm1a2.astarabot.persistent

import com.davidm1a2.astarabot.domain.message.data.IdPlayer
import net.minecraft.util.IItemProvider

class ListingHelper(private val listings: Listings) {
    fun add(seller: IdPlayer, item: IItemProvider, count: Int, price: Int): String {
        val isUpdating = listings.get(seller, item) != null
        return if (isUpdating || listings.count(seller) < MAX_LISTINGS) {
            val listing = Listing(seller, item.asItem(), count, price)
            listings.add(listing)
            if (isUpdating) {
                "Successfully updated the price of ${item.asItem().name.formattedText} to $price diamonds"
            } else {
                "Successfully listed ${item.asItem().name.formattedText} for $price diamonds"
            }
        } else {
            "Failed to add listing for ${item.asItem().name.formattedText}. Limit of $MAX_LISTINGS listings exceeded"
        }
    }

    fun remove(seller: IdPlayer, item: IItemProvider): String {
        val removedSuccessfully = listings.remove(seller, item)
        return if (removedSuccessfully) {
            "Successfully removed listing for ${item.asItem().name.formattedText}s"
        } else {
            "You don't have any ${item.asItem().name.formattedText} listed"
        }
    }

    fun removeAll(seller: IdPlayer): String {
        listings.removeAll(seller)
        return "Successfully removed all store listings"
    }

    fun list(seller: IdPlayer): Set<Listing> {
        return listings.get(seller) ?: emptySet()
    }

    fun list(item: IItemProvider): Set<Listing> {
        return listings.get(item) ?: emptySet()
    }

    companion object {
        private const val MAX_LISTINGS = 40
    }
}