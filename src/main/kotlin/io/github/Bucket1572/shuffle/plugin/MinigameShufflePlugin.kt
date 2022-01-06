package io.github.Bucket1572.shuffle.plugin

import io.github.Bucket1572.shuffle.plugin.minigame.prepare.PrepareUtility
import io.github.monun.kommand.kommand
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

class MinigameShufflePlugin: JavaPlugin() {
    override fun onEnable() {
        registerCommands()
    }

    private fun registerCommands() = kommand {
    }
}