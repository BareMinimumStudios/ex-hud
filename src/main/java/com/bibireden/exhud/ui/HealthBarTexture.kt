package com.bibireden.exhud.ui

enum class HealthBarTexture {
    Empty,
    Normal,
    Poisoned,
    Withered,
    Freezing,
    Absorption,
    Cold,
    Warm;

    val xPos: Int = this.ordinal * 8
}