package com.lvngx.gamespace

data class GameItem(
    val id: String,
    val name: String,
    val packageName: String,
    val category: String,
    val isFavorite: Boolean = false
)

enum class PerformanceMode {
    ECO, BALANCED, ULTRA_BOOST
}

object GameSpaceState {
    var macroCps: Int = 15
    var isMacroLocked: Boolean = false
    var isCrosshairEnabled: Boolean = false
    var performanceMode: String = "Balanced"
}