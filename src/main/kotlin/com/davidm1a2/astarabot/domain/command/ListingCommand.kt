package com.davidm1a2.astarabot.domain.command

import com.davidm1a2.astarabot.domain.IdPlayer
import com.davidm1a2.astarabot.domain.listing.ListingHelper
import com.davidm1a2.astarabot.domain.listing.PriceType
import com.davidm1a2.astarabot.domain.message.MessageDispatcher
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.BoolArgumentType.bool
import com.mojang.brigadier.arguments.BoolArgumentType.getBool
import com.mojang.brigadier.arguments.IntegerArgumentType.getInteger
import com.mojang.brigadier.arguments.IntegerArgumentType.integer
import com.mojang.brigadier.arguments.StringArgumentType.*
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import net.minecraft.client.Minecraft
import net.minecraft.item.Items
import net.minecraft.util.ResourceLocation
import net.minecraftforge.registries.ForgeRegistries

class ListingCommand(private val sender: MessageDispatcher, private val listingHelper: ListingHelper) : BotCommand {
    override fun register(dispatcher: CommandDispatcher<IdPlayer>) {
        val findSubcommand = literal<IdPlayer>("find")
            .then(
                argument<IdPlayer, String>("item", word())
                    .then(
                        argument<IdPlayer, Boolean>("hideOffline", bool())
                            .executes {
                                val itemName = getString(it, "item")
                                val hideOffline = getBool(it, "hideOffline")
                                showListings(it.source, itemName, hideOffline)
                                1
                            }
                    )
                    .executes {
                        val itemName = getString(it, "item")
                        showListings(it.source, itemName)
                        1
                    }
            )

        val removeSubcommand = literal<IdPlayer>("remove")
            .then(
                argument<IdPlayer, String>("item", word())
                    .executes {
                        val itemName = getString(it, "item")
                        if (itemName == "ALL") {
                            val result = listingHelper.removeAll(it.source)
                            sender.send(it.source, result)
                        } else {
                            val item = ForgeRegistries.ITEMS.getValue(ResourceLocation("minecraft", itemName))
                            if (item == null || item == Items.AIR) {
                                sender.send(it.source, "The requested item '$itemName' is not a valid minecraft item")
                            } else {
                                val result = listingHelper.remove(it.source, item)
                                sender.send(it.source, result)
                            }
                        }
                        1
                    }
            )
            .executes {
                val result = listingHelper.removeAll(it.source)
                sender.send(it.source, result)
                1
            }

        val mineSubcommand = literal<IdPlayer>("mine")
            .executes {
                val listingSet = listingHelper.list(it.source)
                if (listingSet.isEmpty()) {
                    sender.send(it.source, "You have no item listings")
                } else {
                    listingSet.sortedBy { entry -> entry.item.name.formattedText }.forEach { entry ->
                        sender.send(it.source, "Selling ${entry.item.name.formattedText} for ${entry.price} ${entry.priceType.friendlyName}")
                    }
                }
                1
            }

        val buySubcommand = literal<IdPlayer>("buy")
            .then(
                argument<IdPlayer, String>("item", word())
                    .then(
                        argument<IdPlayer, String>("player", word())
                            .then(
                                argument<IdPlayer, Int>("multiplier", integer())
                                    .executes {
                                        val itemName = getString(it, "item")
                                        val sellerName = getString(it, "player")
                                        val multiplier = getInteger(it, "multiplier")
                                        buyItem(it.source, itemName, sellerName, multiplier)
                                        1
                                    }
                            )
                            .executes {
                                val itemName = getString(it, "item")
                                val sellerName = getString(it, "player")
                                buyItem(it.source, itemName, sellerName)
                                1
                            }
                    )
            )

        val sellSubcommand = literal<IdPlayer>("sell")
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

        dispatcher.register(
            literal<IdPlayer>("listing")
                .then(findSubcommand)
                .then(removeSubcommand)
                .then(mineSubcommand)
                .then(buySubcommand)
                .then(sellSubcommand)
        )

        dispatcher.register(
            literal<IdPlayer>("l")
                .then(findSubcommand)
                .then(removeSubcommand)
                .then(mineSubcommand)
                .then(buySubcommand)
                .then(sellSubcommand)
        )
    }

    private fun showListings(player: IdPlayer, itemName: String, hideOffline: Boolean = false) {
        val item = ForgeRegistries.ITEMS.getValue(ResourceLocation("minecraft", itemName))
        if (item == null || item == Items.AIR) {
            sender.send(player, "The requested item '$itemName' is not a valid minecraft item")
        } else {
            val listings = listingHelper.list(item).filter { listing ->
                if (hideOffline) {
                    Minecraft.getInstance().connection?.getPlayerInfo(listing.seller.id) != null
                } else {
                    true
                }
            }
            if (listings.isEmpty()) {
                sender.send(player, "This item is not currently being sold")
            } else {
                listings.forEach { listing ->
                    sender.send(player, "${listing.seller.name}: ${listing.count} for ${listing.price} ${listing.priceType.friendlyName}")
                }
            }
        }
    }

    private fun buyItem(player: IdPlayer, itemName: String, sellerName: String, multiplier: Int = 1) {
        val item = ForgeRegistries.ITEMS.getValue(ResourceLocation("minecraft", itemName))
        if (item == null || item == Items.AIR) {
            sender.send(player, "The requested item '$itemName' is not a valid minecraft item")
        } else {
            val sellerPlayer = Minecraft.getInstance().connection?.getPlayerInfo(sellerName)?.gameProfile
            if (sellerPlayer == null) {
                sender.send(player, "$sellerName is offline")
            } else {
                if (multiplier < 1) {
                    sender.send(player, "Multiplier must be a positive integer")
                } else {
                    val sellerIdPlayer = IdPlayer(sellerName, sellerPlayer.id)
                    val listing = listingHelper.get(sellerIdPlayer, item)
                    if (listing == null) {
                        sender.send(player, "$sellerName is not currently selling $itemName(s)")
                    } else {
                        sender.send(player, "Meet $sellerName in /world with ${listing.price * multiplier} ${listing.priceType.friendlyName}")
                        sender.send(sellerIdPlayer, "Meet ${player.name} in /world with ${listing.count * multiplier} $itemName(s)")
                    }
                }
            }
        }
    }
}