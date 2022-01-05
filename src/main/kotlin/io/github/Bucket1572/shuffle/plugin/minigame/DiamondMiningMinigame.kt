package io.github.Bucket1572.shuffle.plugin.minigame

import net.kyori.adventure.text.Component
import org.bukkit.Server
import org.bukkit.entity.Player

class DiamondMiningMinigame(
    server: Server, name: String, summary: String
): Minigame(server, name, summary) {
    override fun getDescription(): Component {
        TODO("Not yet implemented")
    }

    override fun judge(player: Player): Boolean {
        TODO("Not yet implemented")
    }

    override fun reward(player: Player) {
        TODO("Not yet implemented")
    }

    override fun punish(player: Player) {
        TODO("Not yet implemented")
    }
}