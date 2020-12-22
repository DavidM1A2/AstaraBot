package com.davidm1a2.astarabot.domain.command

import com.davidm1a2.astarabot.domain.IdPlayer
import com.davidm1a2.astarabot.domain.listing.ListingHelper
import com.davidm1a2.astarabot.domain.message.MessageDispatcher
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.BoolArgumentType.bool
import com.mojang.brigadier.arguments.BoolArgumentType.getBool
import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.arguments.StringArgumentType.word
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import net.minecraft.client.Minecraft
import net.minecraft.item.Items
import net.minecraft.util.ResourceLocation
import net.minecraftforge.registries.ForgeRegistries

class ListingCommand(private val sender: MessageDispatcher, private val listingHelper: ListingHelper) : BotCommand {
    override fun register(dispatcher: CommandDispatcher<IdPlayer>) {
        // listing find <item> <hideOffline=false> -> Finds all listings for a given item
        // listing remove <item=ALL> -> Removes one/all listings for an item
        // listing mine -> prints out your listings
        dispatcher.register(
            literal<IdPlayer>("listing")
                .then(
                    literal<IdPlayer>("find")
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
                )
                .then(
                    literal<IdPlayer>("remove")
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
                )
                .then(
                    literal<IdPlayer>("mine")
                        .executes {
                            val listingSet = listingHelper.list(it.source)
                            if (listingSet.isEmpty()) {
                                sender.send(it.source, "You have no item listings")
                            } else {
                                listingSet.sortedBy { entry -> entry.item.name.formattedText }.forEach { entry ->
                                    sender.send(it.source, "Selling ${entry.item.name.formattedText} for ${entry.price} diamonds")
                                }
                            }
                            1
                        }
                )
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
                    sender.send(player, "${listing.seller.name}: ${listing.count} for ${listing.price} diamond(s)")
                }
            }
        }
    }
}