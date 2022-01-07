package io.github.Bucket1572.shuffle.plugin

import io.github.Bucket1572.shuffle.plugin.minigame.SplendidGems
import io.github.Bucket1572.shuffle.plugin.minigame.description.DescriptionUtility
import io.github.Bucket1572.shuffle.plugin.score.ScoreHandlingUtility
import io.github.Bucket1572.shuffle.plugin.tag.ColorTag
import io.github.Bucket1572.shuffle.plugin.tag.getTextColor
import io.github.monun.kommand.kommand
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

class MinigameShufflePlugin: JavaPlugin() {
    override fun onEnable() {
        registerCommands()
    }

    private fun registerCommands() = kommand {
        register("gems") {
            executes {
                ScoreHandlingUtility.initScoreBoard(server)
                val minigame = SplendidGems(server)
                minigame.start()
            }
        }
    }
}