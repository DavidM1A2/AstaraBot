package com.davidm1a2.astarabot.domain.message

import com.davidm1a2.astarabot.domain.message.data.IdPlayer
import com.davidm1a2.astarabot.domain.message.processor.MessageDispatcher
import com.davidm1a2.astarabot.persistent.ListingHelper
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.BoolArgumentType.bool
import com.mojang.brigadier.arguments.BoolArgumentType.getBool
import com.mojang.brigadier.arguments.StringArgumentType.*
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.I18n
import net.minecraft.util.ResourceLocation
import net.minecraftforge.registries.ForgeRegistries
import kotlin.math.max

class CommandProcessor(private val sender: MessageDispatcher, listingHelper: ListingHelper) {
    private val dispatcher: CommandDispatcher<IdPlayer> = CommandDispatcher()

    init {
        // help -> prints out bot help
        dispatcher.register(
            literal<IdPlayer>("help")
                .executes {
                    sender.send(it.source, "listings")
                    sender.send(it.source, "listing find <item> <includeOffline=true>")
                    sender.send(it.source, "listing remove <item=ALL>")
                    sender.send(it.source, "sell <item> <count> <price in diamonds>")
                    sender.send(it.source, "buy <item> <player> <multiplier=1>")
                    sender.send(it.source, "msg <player> <message>")
                    1
                }
        )

        // listings -> prints out your listings
        dispatcher.register(
            literal<IdPlayer>("listings")
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

        // listing find <item> <hideOffline=true> -> Finds all listings for a given item
        // listing remove <item=ALL> -> Removes one/all listings for an item
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
                                            val item = ForgeRegistries.ITEMS.getValue(ResourceLocation("minecraft", itemName))
                                            if (item == null) {
                                                sender.send(it.source, "The requested item '$itemName' is not a valid minecraft item")
                                            } else {
                                                val listings = listingHelper.list(item).filter { listing ->
                                                    if (hideOffline) {
                                                        Minecraft.getInstance().connection?.getPlayerInfo(listing.seller.id) != null
                                                    } else {
                                                        true
                                                    }
                                                }
                                                if (listings.isEmpty()) {
                                                    sender.send(it.source, "This item is not currently being sold")
                                                } else {
                                                    listings.forEach { listing ->
                                                        sender.send(it.source, "${listing.seller.name}: ${listing.count} for ${listing.price} diamond(s)")
                                                    }
                                                }
                                            }
                                            1
                                        }
                                )
                                .executes {
                                    val itemName = getString(it, "item")
                                    val item = ForgeRegistries.ITEMS.getValue(ResourceLocation("minecraft", itemName))
                                    if (item == null) {
                                        sender.send(it.source, "The requested item '$itemName' is not a valid minecraft item")
                                    } else {
                                        val listings = listingHelper.list(item)
                                        if (listings.isEmpty()) {
                                            sender.send(it.source, "This item is not currently being sold")
                                        } else {
                                            listings.forEach { listing ->
                                                sender.send(it.source, "${listing.seller.name}: ${listing.count} for ${listing.price} diamond(s)")
                                            }
                                        }
                                    }
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
                                        if (item == null) {
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
        )

        // msg <player> <message> - Messages a given player anonymously
        dispatcher.register(
            literal<IdPlayer>("msg")
                .then(
                    argument<IdPlayer, String>("player", word())
                        .then(argument<IdPlayer, String>("message", greedyString())
                            .executes {
                                val playerName = getString(it, "player")
                                val message = getString(it, "message")
                                if (Minecraft.getInstance().player?.gameProfile?.name?.equals(playerName, ignoreCase = true) == true) {
                                    sender.send(it.source, "You can't message me...")
                                } else {
                                    val targetPlayer = Minecraft.getInstance().connection?.getPlayerInfo(playerName)?.gameProfile
                                    if (targetPlayer == null) {
                                        sender.send(it.source, "$playerName is offline")
                                    } else {
                                        sender.send(IdPlayer(targetPlayer.name, targetPlayer.id), "<Anonymous> $message")
                                    }
                                }
                                1
                            })
                )
        )
    }

    fun process(player: IdPlayer, command: String) {
        if (Minecraft.getInstance().player?.gameProfile?.id != player.id) {
            try {
                dispatcher.execute(command, player)
            } catch (e: CommandSyntaxException) {
                sender.send(player, e.rawMessage.string)

                // Copied from  Vanilla
                if (e.input != null && e.cursor >= 0) {
                    val errorPos = e.input.length.coerceAtMost(e.cursor)
                    var errorStr = ""
                    if (errorPos > 10) {
                        errorStr = "..."
                    }
                    errorStr += e.input.substring(max(errorPos - 10, 0), errorPos)
                    errorStr += I18n.format("command.context.here")
                    sender.send(player, errorStr)
                }
            }
        }
    }
}