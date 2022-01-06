package io.github.Bucket1572.shuffle.plugin.minigame

import io.github.Bucket1572.shuffle.plugin.result.MinigameResult
import net.kyori.adventure.text.Component
import org.bukkit.Server
import org.bukkit.entity.Player

class SplendidGems(private val server: Server):
    Minigame(
        server, "반짝이는 보석들", "다이아몬드와 에메랄드를 가장 많이 모으세요."
    )
{
    override fun prepare() {
        TODO("Not yet implemented")
    }

    override fun getDescription(): Component {
        TODO("Not yet implemented")
    }

    override fun judge(player: Player): MinigameResult {
        TODO("Not yet implemented")
    }

    override fun winResponse(player: Player) {
        TODO("Not yet implemented")
    }

    override fun loseResponse(player: Player) {
        TODO("Not yet implemented")
    }

    override fun failResponse(player: Player) {
        TODO("Not yet implemented")
    }

    override fun cleanUp() {
        TODO("Not yet implemented")
    }
}