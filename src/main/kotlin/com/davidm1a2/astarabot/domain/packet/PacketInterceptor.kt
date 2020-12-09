package com.davidm1a2.astarabot.domain.packet

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import net.minecraft.network.IPacket
import net.minecraftforge.client.event.ClientPlayerNetworkEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.SubscribeEvent

class PacketInterceptor {
    private var initialized = false

    @SubscribeEvent
    fun onPlayerLoggedIn(event: ClientPlayerNetworkEvent.LoggedInEvent) {
        if (!initialized) {
            initialized = true

            val pipeline = event.networkManager?.channel()?.pipeline()
            if (pipeline != null) {
                pipeline.addBefore("packet_handler", "time_packet_interceptor", PacketHandler())
            } else {
                println("Failed to initialize packet interceptor")
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onPlayerLoggedOut(event: ClientPlayerNetworkEvent.LoggedOutEvent) {
        initialized = false
    }

    private class PacketHandler : ChannelInboundHandlerAdapter() {
        override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
            if (msg is IPacket<*>) {
                MinecraftForge.EVENT_BUS.post(ReceivePacketEvent(msg))
            }
            super.channelRead(ctx, msg)
        }
    }
}