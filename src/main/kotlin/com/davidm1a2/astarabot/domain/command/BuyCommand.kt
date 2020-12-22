package com.davidm1a2.astarabot.domain.command

import com.davidm1a2.astarabot.domain.IdPlayer
import com.davidm1a2.astarabot.domain.listing.ListingHelper
import com.davidm1a2.astarabot.domain.message.MessageDispatcher
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType.getInteger
import com.mojang.brigadier.arguments.IntegerArgumentType.integer
import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.arguments.StringArgumentType.word
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import net.minecraft.client.Minecraft
import net.minecraft.item.Items
import net.minecraft.util.ResourceLocation
import net.minecraftforge.registries.ForgeRegistries

class BuyCommand(private val sender: MessageDispatcher, private val listingHelper: ListingHelper) : BotCommand {
    override fun register(dispatcher: CommandDispatcher<IdPlayer>) {
        // buy <item> <player> <multiplier=1>
        dispatcher.register(
            literal<IdPlayer>("buy")
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
        )
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
                        sender.send(player, "Meet $sellerName in /world with ${listing.price * multiplier} diamond(s)")
                        sender.send(sellerIdPlayer, "Meet ${player.name} in /world with ${listing.count * multiplier} $itemName(s)")
                    }
                }
            }
        }
    }
}