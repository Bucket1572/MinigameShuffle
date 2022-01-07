package io.github.Bucket1572.shuffle.plugin.common

import kotlinx.coroutines.flow.asFlow
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory

fun PlayerInventory.any(predicate: (ItemStack?) -> Boolean): Boolean {
    for (i in 0..3) {
        if (predicate(this.armorContents[i])) return true
    }
    for (i in 0..35) {
        if (predicate(this.storageContents[i])) return true
    }
    return false
}

fun PlayerInventory.count(predicate: (ItemStack?) -> Boolean): Int {
    var count = 0
    for (i in 0..3) {
        if (predicate(this.armorContents[i])) count++
    }
    for (i in 0..35) {
        if (predicate(this.storageContents[i])) count++
    }
    return count
}

fun PlayerInventory.removeAll(predicate: (ItemStack?) -> Boolean): Boolean {
    var removedFlag = false
    val newArmorContents = arrayOfNulls<ItemStack?>(4)
    for (i in 0..3) {
        if (predicate(this.armorContents[i])) {
            removedFlag = true
            newArmorContents[i] = null
        } else {
            newArmorContents[i] = this.armorContents[i]
        }
    }
    this.setArmorContents(newArmorContents)

    for (i in 0..35) {
        if (predicate(this.storageContents[i])) {
            removedFlag = true
            this.setItem(i, null)
        }
    }
    return removedFlag
}