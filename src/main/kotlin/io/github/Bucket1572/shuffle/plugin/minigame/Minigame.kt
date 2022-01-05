package io.github.Bucket1572.shuffle.plugin.minigame

import io.github.Bucket1572.shuffle.plugin.color.ColorTag
import io.github.Bucket1572.shuffle.plugin.color.getTextColor
import io.github.monun.heartbeat.coroutines.HeartbeatScope
import io.github.monun.heartbeat.coroutines.Suspension
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.Server
import org.bukkit.entity.Player
import java.time.Duration

abstract class Minigame(
    private val server: Server, private val name: String, private val summary: String,
) {
    fun start() {
        broadcast()
        describe()
        startCountDown()
    }

    private fun broadcast() {
        server.showTitle(
            Title.title(
                Component.text(name, ColorTag.MINIGAME_TITLE.getTextColor()),
                Component.text(summary, ColorTag.MINIGAME_SUBTITLE.getTextColor()),
                Title.Times.of(Duration.ofSeconds(1L), Duration.ofSeconds(2L), Duration.ofSeconds(1L))
            )
        )
    }

    private fun describe() {
        val description = getDescription()
        server.broadcast(description)
    }

    private fun startCountDown() {
        HeartbeatScope().launch {
            val suspension = Suspension()
            suspension.delay(5 * 60 * 1000L)
            end()
        }
    }

    abstract fun getDescription(): Component

    private fun end() {
        server.onlinePlayers.forEach { player ->
            run {
                val judgement = judge(player)
                if (judgement) {
                    reward(player)
                } else {
                    punish(player)
                }
            }
        }
    }

    abstract fun judge(player: Player): Boolean

    abstract fun reward(player: Player)

    abstract fun punish(player: Player)
}