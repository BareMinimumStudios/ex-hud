package com.bibireden.exhud.compat

import net.fabricmc.loader.api.FabricLoader

object CompatUtil {
    @JvmStatic
    fun isModLoaded(id: String) = FabricLoader.getInstance().isModLoaded(id)

    @JvmStatic
    fun isThermooLoaded() = isModLoaded("thermoo")

    @JvmStatic
    fun isDehydrationLoaded() = isModLoaded("dehydration")
}