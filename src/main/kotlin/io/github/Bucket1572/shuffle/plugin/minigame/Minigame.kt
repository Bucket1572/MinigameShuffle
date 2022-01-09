package io.github.Bucket1572.shuffle.plugin.minigame

import io.github.Bucket1572.shuffle.plugin.common.reward
import io.github.Bucket1572.shuffle.plugin.minigame.cleanup.cleanUp
import io.github.Bucket1572.shuffle.plugin.minigame.description.DescriptionUtility
import io.github.Bucket1572.shuffle.plugin.minigame.prepare.PrepareUtility
import io.github.Bucket1572.shuffle.plugin.minigame.prepare.feed
import io.github.Bucket1572.shuffle.plugin.result.MinigameResult
import io.github.Bucket1572.shuffle.plugin.score.fail
import io.github.Bucket1572.shuffle.plugin.score.win
import io.github.Bucket1572.shuffle.plugin.tag.ColorTag
import io.github.Bucket1572.shuffle.plugin.tag.getTextColor
import io.github.monun.heartbeat.coroutines.HeartbeatScope
import io.github.monun.heartbeat.coroutines.Suspension
import kotlinx.coroutines.launch
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.Material
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.time.Duration

abstract class Minigame(
    private val server: Server, val name: String, private val summary: String, private val iconMaterial: Material,
    private val details: List<String>, private val failConditions: List<String>
) {
    val minigameLength = 20

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

    private fun prepare() {
        val minigameIcon = PrepareUtility.setMinigameIcon(iconMaterial, name, summary)
        val helperTools = getHelperTools()
        val shulkerPackage = PrepareUtility.shulkerPackage(minigameIcon, helperTools)
        server.feed(shulkerPackage)

        additionalPreparation()
    }

    abstract fun getHelperTools(): List<ItemStack>

    abstract fun additionalPreparation()

    private fun startCountDown() {
        val timeLength = minigameLength
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

    private fun getDescription(): Component {
        val title = Component.text(name).style {
            it.decoration(TextDecoration.BOLD, TextDecoration.State.TRUE)
            it.color(ColorTag.MINIGAME_TITLE.getTextColor())
        }
        val rewards = getRewards()
        return DescriptionUtility.generateDescription(title, details, failConditions, rewards)
    }

    abstract fun getRewards(): List<ItemStack>

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

    abstract fun additionalCleanUp()

    private fun winResponse(player: Player) {
        player.sendActionBar(Component.text("승리!").color(ColorTag.WIN.getTextColor()))
        player.win()
        player.reward(getRewards())
    }

    private fun loseResponse(player: Player) {
        player.sendActionBar(Component.text("패배...").color(ColorTag.LOSE.getTextColor()))
    }

    private fun failResponse(player: Player) {
        player.sendActionBar(Component.text("⚠ 실격 ⚠").color(ColorTag.FAIL.getTextColor()))
        player.fail()
    }

    private fun cleanUp() {
        server.cleanUp(this)
        additionalCleanUp()
    }
}