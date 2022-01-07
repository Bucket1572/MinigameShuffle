package io.github.Bucket1572.shuffle.plugin.minigame.prepare

import io.github.Bucket1572.shuffle.plugin.tag.*
import io.github.monun.invfx.internal.frame.InvFrameImpl
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.Server
import org.bukkit.block.ShulkerBox
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import java.util.stream.Stream

object PrepareUtility {
    fun shulkerPackage(minigameIcon: ItemStack, helperTools: List<ItemStack>): ItemStack {
        assert(helperTools.size <= 5)

        val shulkerBox: ItemStack = ItemStack(Material.SHULKER_BOX, 1)
        shulkerBox.editMeta {
            val blockStateMeta = it as BlockStateMeta
            val box = blockStateMeta.blockState as ShulkerBox
            val inventory = box.inventory
            helperTools.forEachIndexed { index, itemStack ->
                inventory.setItem(index + 12, itemStack)
            }
            inventory.setItem(10, minigameIcon)
            blockStateMeta.blockState = box
        }
        return shulkerBox
    }

    fun setMinigameIcon(material: Material, gameName: String, gameSummary: String): ItemStack {
        val itemStack = ItemStack(material, 1)
        itemStack.editMeta { itemMeta ->
            itemMeta.displayName(
                Component.text(gameName).style {
                    it.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                    it.color(ColorTag.MINIGAME_TITLE.getTextColor())
                }
            )
            itemMeta.lore(
                listOf(
                    Component.text(gameSummary).style {
                        it.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        it.color(ColorTag.MINIGAME_SUBTITLE.getTextColor())
                    }
                )
            )
        }
        itemStack.makeIcon()
        return itemStack
    }
}

fun Server.feed(itemStack: ItemStack) {
    this.onlinePlayers.forEach {
        it.inventory.setItem(8, itemStack)
    }
}