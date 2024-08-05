package com.bibireden.exhud.compat.appleskin

import com.bibireden.exhud.ExHUD
import squeek.appleskin.api.AppleSkinApi
import squeek.appleskin.api.event.HUDOverlayEvent

class AppleSkinEventHandler : AppleSkinApi {
    override fun registerEvents() {
        HUDOverlayEvent.HealthRestored.EVENT.register { it.isCanceled = ExHUD.renderCustomHealthbar() }
    }
}