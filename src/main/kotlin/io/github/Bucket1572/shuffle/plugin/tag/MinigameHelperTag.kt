package io.github.Bucket1572.shuffle.plugin.tag

import io.github.Bucket1572.shuffle.plugin.minigame.Minigame
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

enum class MinigameHelperTag(val tagName: NamespacedKey) {
    MINIGAME_HELPER(NamespacedKey.fromString("help_minigame")!!),
    MINIGAME_ICON(NamespacedKey.fromString("is_minigame_icon")!!)
}

fun ItemStack.makeIcon() {
    this.editMeta {
        val iconTag = it.persistentDataContainer.getOrDefault(
            MinigameHelperTag.MINIGAME_ICON.tagName, PersistentDataType.BYTE, 0
        )
        if (iconTag == 0.toByte()) {
            it.persistentDataContainer.set(
                MinigameHelperTag.MINIGAME_ICON.tagName, PersistentDataType.BYTE, 1
            )
        }
    }
}

fun ItemStack?.isIcon(): Boolean {
    if (this != null) {
        return this.itemMeta.persistentDataContainer.getOrDefault(
            MinigameHelperTag.MINIGAME_ICON.tagName, PersistentDataType.BYTE, 0
        ) == 1.toByte()
    }
    return false
}

fun ItemStack.makeHelper(minigame: Minigame) {
    assert(
        this.itemMeta.persistentDataContainer.getOrDefault(
            MinigameHelperTag.MINIGAME_HELPER.tagName, PersistentDataType.STRING, ""
        ) != minigame.name
    )
    this.editMeta {
        it.persistentDataContainer.set(
            MinigameHelperTag.MINIGAME_HELPER.tagName, PersistentDataType.STRING, minigame.name
        )
    }
}

fun ItemStack?.isHelper(minigame: Minigame): Boolean {
    if (this != null) {
        return this.itemMeta.persistentDataContainer.getOrDefault(
            MinigameHelperTag.MINIGAME_HELPER.tagName, PersistentDataType.STRING, ""
        ) == minigame.name
    }
    return false
}