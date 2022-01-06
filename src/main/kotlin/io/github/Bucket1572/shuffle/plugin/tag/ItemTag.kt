package io.github.Bucket1572.shuffle.plugin.tag

import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

enum class ItemTag {
    MINIGAME_HELPER, MINIGAME_ICON
}

fun ItemStack.addTag(itemTag: ItemTag) {
    this.editMeta {
        val shuffleTags = it.persistentDataContainer.getOrDefault(
            NamespacedKey.fromString("shuffle_tag")!!, PersistentDataType.INTEGER_ARRAY, intArrayOf()
        )
        if (!shuffleTags.contains(itemTag.ordinal)) {
            val tagList = shuffleTags.toMutableList()
            tagList.add(itemTag.ordinal)
            tagList.sort()
            val updatedShuffleTags = tagList.toIntArray()
            it.persistentDataContainer.set(
                NamespacedKey.fromString("shuffle_tag")!!, PersistentDataType.INTEGER_ARRAY, updatedShuffleTags
            )
        }
    }
}