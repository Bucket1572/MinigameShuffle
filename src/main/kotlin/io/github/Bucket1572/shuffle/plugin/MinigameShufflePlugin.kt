package io.github.Bucket1572.shuffle.plugin

import io.github.monun.kommand.kommand
import org.bukkit.plugin.java.JavaPlugin

class MinigameShufflePlugin: JavaPlugin() {
    override fun onEnable() {
        registerCommands()
    }

    private fun registerCommands() = kommand {

    }
}