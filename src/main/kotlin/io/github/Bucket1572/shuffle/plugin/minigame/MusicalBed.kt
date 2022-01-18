package io.github.Bucket1572.shuffle.plugin.minigame

import io.github.Bucket1572.shuffle.plugin.MinigameShufflePlugin
import io.github.Bucket1572.shuffle.plugin.common.any
import io.github.Bucket1572.shuffle.plugin.common.count
import io.github.Bucket1572.shuffle.plugin.result.MinigameResult
import io.github.Bucket1572.shuffle.plugin.tag.ColorTag
import io.github.Bucket1572.shuffle.plugin.tag.getTextColor
import io.github.Bucket1572.shuffle.plugin.tag.makeHelper
import io.github.monun.heartbeat.coroutines.HeartbeatScope
import io.github.monun.heartbeat.coroutines.Suspension
import io.github.monun.tap.effect.playFirework
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Firework
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryPickupItemEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerBedEnterEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.*
import kotlin.random.Random

class MusicalBed(private val plugin: MinigameShufflePlugin):
    Minigame(
        plugin.server, "침대 뺏기", "밤이 되는 순간 잠에 드세요.",
        Material.CLOCK,
        listOf(
            "주어진 5분의 시간 내 랜덤한 시점에 갑자기 밤이 찾아옵니다.",
            "이때 다른 누구보다도 빨리 침대에 누워야 합니다.",
            "자연적으로 찾아온 밤에 맞추어 침대에 누워도 인정됩니다."
        ),
        listOf(
            "제한 시간 내에 침대에 눕지 않은 경우"
        )
    ), Listener
{
    private val sleepingRanking = mutableListOf<Player>()

    override fun getHelperTools(): List<ItemStack> {
        return emptyList()
    }

    override fun additionalPreparation() {
        setBedTime()
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    override fun judge(player: Player, rankings: List<Player>): MinigameResult {
        if (player !in sleepingRanking) return MinigameResult.FAIL
        if (player == rankings[0]) return MinigameResult.WIN
        return MinigameResult.LOSE
    }

    override fun getRankings(): List<Player> {
        return sleepingRanking
    }

    override fun additionalCleanUp() {
        PlayerMoveEvent.getHandlerList().unregister(this)
    }

    override fun getRewards(): List<ItemStack> {
        return emptyList()
    }

    private fun setBedTime() {
        val timeUpperLimit = minigameLength - 20L
        val timeLowerLimit = 20L
        HeartbeatScope().launch {
            val suspension = Suspension()
            suspension.delay(Random.nextLong(timeLowerLimit, timeUpperLimit) * 1000)
            plugin.server.worlds.forEach { it.time = 13000 }
        }
    }

    @EventHandler
    fun onSleeping(event: PlayerBedEnterEvent) {
        if (!isGoingToBed(event)) return

        sleepingRanking.add(event.player)
    }

    private fun isGoingToBed(event: PlayerBedEnterEvent): Boolean {
        return (event.bedEnterResult == PlayerBedEnterEvent.BedEnterResult.OK)
                && (event.player !in sleepingRanking)
    }
}