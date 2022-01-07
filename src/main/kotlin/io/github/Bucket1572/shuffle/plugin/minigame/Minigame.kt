package io.github.Bucket1572.shuffle.plugin.minigame

import io.github.Bucket1572.shuffle.plugin.minigame.cleanup.cleanUp
import io.github.Bucket1572.shuffle.plugin.result.MinigameResult
import io.github.Bucket1572.shuffle.plugin.tag.ColorTag
import io.github.Bucket1572.shuffle.plugin.tag.getTextColor
import io.github.monun.heartbeat.coroutines.HeartbeatScope
import io.github.monun.heartbeat.coroutines.Suspension
import kotlinx.coroutines.launch
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.Server
import org.bukkit.entity.Player
import java.time.Duration

abstract class Minigame(
    private val server: Server, public val name: String, protected val summary: String,
) {
    fun start() {
        broadcast()
        describe()
        prepare()
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

    abstract fun prepare()

    private fun startCountDown() {
        val timeLength = 300
        var remainingTime = timeLength
        val bossBar = BossBar.bossBar(
            Component.text(name).color(ColorTag.MINIGAME_TITLE.getTextColor()),
            remainingTime / timeLength.toFloat(), BossBar.Color.GREEN, BossBar.Overlay.PROGRESS
        )
        HeartbeatScope().launch {
            val suspension = Suspension()
            server.showBossBar(bossBar)
            repeat(timeLength) {
                suspension.delay(1000L)
                remainingTime--
                if (remainingTime <= 0) bossBar.progress(0.0f)
                else bossBar.progress(remainingTime / timeLength.toFloat())
            }
            server.hideBossBar(bossBar)
            end()
        }
    }

    abstract fun getDescription(): Component

    private fun end() {
        server.onlinePlayers.forEach { player ->
            when (judge(player, getRankings())) {
                MinigameResult.WIN -> winResponse(player)
                MinigameResult.LOSE -> loseResponse(player)
                MinigameResult.FAIL -> failResponse(player)
            }
        }
        cleanUp()
    }

    abstract fun judge(player: Player, rankings: List<Player>): MinigameResult

    abstract fun getRankings(): List<Player>

    abstract fun winResponse(player: Player)

    abstract fun loseResponse(player: Player)

    abstract fun failResponse(player: Player)

    private fun cleanUp() {
        server.cleanUp(this)
    }
}