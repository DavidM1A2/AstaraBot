package com.davidm1a2.astarabot.dataaccess

import com.davidm1a2.astarabot.domain.IdPlayer
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService
import net.minecraft.client.Minecraft
import net.minecraft.server.MinecraftServer
import net.minecraft.server.management.PlayerProfileCache
import java.io.File
import java.net.Proxy
import java.util.*

class AccountUUIDFinder {
    private val cache: PlayerProfileCache

    init {
        val profileRepository =
            YggdrasilAuthenticationService(Proxy.NO_PROXY, UUID.randomUUID().toString()).createProfileRepository()
        cache = PlayerProfileCache(
            profileRepository,
            File(Minecraft.getInstance().gameDir, MinecraftServer.USER_CACHE_FILE.name)
        )
    }

    fun getAccount(username: String?): IdPlayer? {
        return username?.let {
            cache.getGameProfileForUsername(it)?.let { gameProfile ->
                gameProfile.id?.let { id -> IdPlayer(it, id) }
            }
        }
    }
}