package io.github.Bucket1572.shuffle.plugin.color

import net.kyori.adventure.text.format.TextColor

enum class ColorTag(val red: Int, val green: Int, val blue: Int) {
    SCOREBOARD(255, 0, 0),
    MINIGAME_TITLE(255, 255, 255),
    MINIGAME_SUBTITLE(255, 215, 0),
    MINIGAME_DESCRIPTION(255, 255, 255)
}

fun ColorTag.getTextColor(): TextColor = TextColor.color(red, green, blue)