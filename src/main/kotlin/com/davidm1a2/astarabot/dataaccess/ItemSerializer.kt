package com.davidm1a2.astarabot.dataaccess

import com.google.gson.*
import net.minecraft.item.Item
import net.minecraft.util.ResourceLocation
import net.minecraftforge.registries.ForgeRegistries
import java.lang.reflect.Type

class ItemSerializer : JsonSerializer<Item>, JsonDeserializer<Item> {
    override fun serialize(src: Item, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive(src.registryName.toString())
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Item {
        val itemLocation = json.asString
        return ForgeRegistries.ITEMS.getValue(ResourceLocation(itemLocation))!!
    }
}