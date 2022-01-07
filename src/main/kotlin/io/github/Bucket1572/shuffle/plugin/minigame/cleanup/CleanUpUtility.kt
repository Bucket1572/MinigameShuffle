package io.github.Bucket1572.shuffle.plugin.minigame.cleanup

import io.github.Bucket1572.shuffle.plugin.common.removeAll
import io.github.Bucket1572.shuffle.plugin.minigame.Minigame
import io.github.Bucket1572.shuffle.plugin.tag.isHelper
import io.github.Bucket1572.shuffle.plugin.tag.isIcon
import org.bukkit.NamespacedKey
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

object CleanUpUtility {
    fun removeHelpers(player: Player, minigame: Minigame) {
        player.inventory.removeAll {
            it.isHelper(minigame)
        }
    }

    fun removeIcon(player: Player) {
        player.inventory.removeAll {
            it.isIcon()
        }
    }
}

fun Player.cleanUp(minigame: Minigame) {
    CleanUpUtility.removeHelpers(this, minigame)
    CleanUpUtility.removeIcon(this)
}

fun Server.cleanUp(minigame: Minigame) {
    this.onlinePlayers.forEach {
        it.cleanUp(minigame)
    }
}