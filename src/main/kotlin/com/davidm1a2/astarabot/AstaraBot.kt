package com.davidm1a2.astarabot

import com.davidm1a2.astarabot.message.processor.MessageHandler
import com.davidm1a2.astarabot.message.processor.MessageParser
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext

@Mod(Constants.MOD_ID)
class AstaraBot {
    init {
        val forgeBus = MinecraftForge.EVENT_BUS
        val modBus = FMLJavaModLoadingContext.get().modEventBus

        forgeBus.register(IngameChatHandler(MessageHandler(), MessageParser()))
    }
}