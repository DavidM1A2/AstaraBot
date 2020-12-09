package com.davidm1a2.astarabot.domain.packet

import net.minecraft.network.IPacket
import net.minecraftforge.eventbus.api.Event

class ReceivePacketEvent(val packet: IPacket<*>) : Event()