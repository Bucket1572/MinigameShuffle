package io.github.Bucket1572.shuffle.plugin.minigame.prepare

import io.github.monun.invfx.internal.frame.InvFrameImpl
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.block.ShulkerBox
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta

object PrepareUtility {
    fun feed(player: Player, minigameIcon: ItemStack, helperTools: List<ItemStack>) {
        assert(helperTools.size <= 5)

        val shulkerBox: ItemStack = ItemStack(Material.SHULKER_BOX, 1)
        shulkerBox.editMeta {
            val blockStateMeta = it as BlockStateMeta
            val box = blockStateMeta.blockState as ShulkerBox
            val inventory = box.inventory
            helperTools.forEachIndexed {
                    index, itemStack -> inventory.setItem(index + 12, itemStack)
            }
            inventory.setItem(10, minigameIcon)
            blockStateMeta.blockState = box
        }
        player.inventory.setItem(0, shulkerBox)
    }
}