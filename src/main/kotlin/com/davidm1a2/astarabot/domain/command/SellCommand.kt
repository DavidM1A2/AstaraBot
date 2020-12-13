package com.davidm1a2.astarabot.domain.command

import com.davidm1a2.astarabot.domain.message.data.IdPlayer
import com.davidm1a2.astarabot.domain.message.processor.MessageDispatcher
import com.davidm1a2.astarabot.persistent.ListingHelper
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType.getInteger
import com.mojang.brigadier.arguments.IntegerArgumentType.integer
import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.arguments.StringArgumentType.word
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import net.minecraft.item.Items
import net.minecraft.util.ResourceLocation
import net.minecraftforge.registries.ForgeRegistries

class SellCommand(private val sender: MessageDispatcher, private val listingHelper: ListingHelper) : BotCommand {
    override fun register(dispatcher: CommandDispatcher<IdPlayer>) {
        // sell <item> <count> <price in diamonds>
        dispatcher.register(
            literal<IdPlayer>("sell")
                .then(
                    argument<IdPlayer, String>("item", word())
                        .then(
                            argument<IdPlayer, Int>("count", integer())
                                .then(
                                    argument<IdPlayer, Int>("priceInDiamonds", integer())
                                        .executes {
                                            val itemName = getString(it, "item")
                                            val item = ForgeRegistries.ITEMS.getValue(ResourceLocation("minecraft", itemName))
                                            if (item == null || item == Items.AIR) {
                                                sender.send(it.source, "The requested item '$itemName' is not a valid minecraft item")
                                            } else {
                                                val count = getInteger(it, "count")
                                                val priceInDiamonds = getInteger(it, "priceInDiamonds")
                                                when {
                                                    count < 1 -> sender.send(it.source, "Must sell at least one $itemName")
                                                    priceInDiamonds < 1 -> sender.send(it.source, "Must have a price of at least one diamond")
                                                    else -> {
                                                        val result = listingHelper.add(it.source, item, count, priceInDiamonds)
                                                        sender.send(it.source, result)
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