package com.davidm1a2.astarabot.domain.command

import com.davidm1a2.astarabot.domain.IdPlayer
import com.davidm1a2.astarabot.domain.listing.ListingHelper
import com.davidm1a2.astarabot.domain.listing.PriceType
import com.davidm1a2.astarabot.domain.message.MessageDispatcher
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType.getInteger
import com.mojang.brigadier.arguments.IntegerArgumentType.integer
import com.mojang.brigadier.arguments.StringArgumentType.*
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import net.minecraft.item.Items
import net.minecraft.util.ResourceLocation
import net.minecraftforge.registries.ForgeRegistries

class SellCommand(private val sender: MessageDispatcher, private val listingHelper: ListingHelper) : BotCommand {
    override fun register(dispatcher: CommandDispatcher<IdPlayer>) {
        // sell <item> <count> <price>[type=d/db]
        dispatcher.register(
            literal<IdPlayer>("sell")
                .then(
                    argument<IdPlayer, String>("item", word())
                        .then(
                            argument<IdPlayer, Int>("count", integer())
                                .then(
                                    argument<IdPlayer, String>("price", string())
                                        .executes {
                                            val itemName = getString(it, "item")
                                            val item = ForgeRegistries.ITEMS.getValue(ResourceLocation("minecraft", itemName))
                                            if (item == null || item == Items.AIR) {
                                                sender.send(it.source, "The requested item '$itemName' is not a valid minecraft item")
                                            } else {
                                                val count = getInteger(it, "count")
                                                val priceRaw = getString(it, "price")
                                                val priceType = PriceType.values().find { type -> priceRaw.endsWith(type.suffix) } ?: PriceType.DIAMONDS
                                                val price = priceRaw.substringBefore(priceType.suffix).toIntOrNull()
                                                if (price == null) {
                                                    sender.send(it.source, "Invalid price $priceRaw. Price must be an integer, or an integer followed by a unit, eg: 1, 3db, 2d, etc")
                                                } else {
                                                    when {
                                                        count < 1 -> sender.send(it.source, "Must sell at least one $itemName")
                                                        price < 1 -> sender.send(it.source, "Must have a price of at least one ${priceType.friendlyName}")
                                                        else -> {
                                                            val result = listingHelper.add(it.source, item, count, price, priceType)
                                                            sender.send(it.source, result)
                                                        }
                                                    }
                                                }
                                            }
                                            1
                                        }
                                )
                        )
                )
        )
    }
}