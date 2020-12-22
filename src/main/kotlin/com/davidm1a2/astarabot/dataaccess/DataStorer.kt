package com.davidm1a2.astarabot.dataaccess

import com.davidm1a2.astarabot.domain.listing.Listings
import com.google.gson.GsonBuilder
import net.minecraft.item.Item
import net.minecraft.network.NetworkManager
import net.minecraftforge.client.event.ClientPlayerNetworkEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import java.io.File
import java.net.InetSocketAddress

class DataStorer(private val listings: Listings) {
    @SubscribeEvent
    fun onPlayerLoginEvent(event: ClientPlayerNetworkEvent.LoggedInEvent) {
        val serverIp = getServerIp(event.networkManager)

        readFromFile(File("./AstaraBot/server-$serverIp.json"))
    }

    @SubscribeEvent
    fun onPlayerLogoutEvent(event: ClientPlayerNetworkEvent.LoggedOutEvent) {
        val serverIp = getServerIp(event.networkManager)

        writeToFile(File("./AstaraBot/server-$serverIp.json"))
    }

    private fun getServerIp(manager: NetworkManager?): String {
        return (manager?.channel()?.remoteAddress() as? InetSocketAddress)
            ?.hostName
            ?.toString()
            ?.replace(".", "-")
            ?: "local"
    }

    private fun readFromFile(file: File) {
        if (file.exists()) {
            val json = file.readText()
            val storage = gson.fromJson(json, AstaraBotDiskStorage::class.java)
            listings.loadFrom(storage.listings)
        }
    }

    private fun writeToFile(file: File) {
        val storage = AstaraBotDiskStorage(listings)
        val json = gson.toJson(storage)
        file.mkdirs()
        file.delete()
        file.createNewFile()
        file.writeText(json)
    }

    companion object {
        private val gson = GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .registerTypeHierarchyAdapter(Item::class.java, ItemSerializer())
            .enableComplexMapKeySerialization()
            .create()
    }
}