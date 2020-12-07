package com.davidm1a2.astarabot.persistent

import com.davidm1a2.astarabot.domain.CommandResult
import net.minecraft.util.IItemProvider
import java.util.*

class Subscriptions {
    private val itemSubscriptions: MutableMap<UUID, MutableSet<SubscriptionEntry>> = mutableMapOf()

    fun add(playerId: UUID, item: IItemProvider): CommandResult {
        val subscriptions = itemSubscriptions.computeIfAbsent(playerId) { mutableSetOf() }
        return if (subscriptions.size < MAX_SUBSCRIPTIONS) {
            subscriptions.add(SubscriptionEntry(item.asItem()))
            CommandResult("Successfully subscribed to ${item.asItem().name.formattedText} store notifications")
        } else {
            CommandResult("Failed to subscribe to ${item.asItem().name.formattedText} store notifications. Limit of $MAX_SUBSCRIPTIONS subscriptions exceeded")
        }
    }

    fun remove(playerId: UUID, item: IItemProvider): CommandResult {
        val unsubscribedSuccessfully = itemSubscriptions[playerId]?.remove(SubscriptionEntry(item.asItem())) == true
        return if (unsubscribedSuccessfully) {
            CommandResult("Successfully unsubscribed from ${item.asItem().name.formattedText} store notifications")
        } else {
            CommandResult("You aren't subscribed to ${item.asItem().name.formattedText}s")
        }
    }

    fun removeAll(playerId: UUID): CommandResult {
        itemSubscriptions.remove(playerId)
        return CommandResult("Successfully unsubscribed from all store notifications")
    }

    companion object {
        private const val MAX_SUBSCRIPTIONS = 10
    }
}