package io.github.Bucket1572.shuffle.plugin.minigame

import io.github.Bucket1572.shuffle.plugin.MinigameShufflePlugin
import io.github.Bucket1572.shuffle.plugin.common.any
import io.github.Bucket1572.shuffle.plugin.common.count
import io.github.Bucket1572.shuffle.plugin.result.MinigameResult
import io.github.Bucket1572.shuffle.plugin.tag.ColorTag
import io.github.Bucket1572.shuffle.plugin.tag.getTextColor
import io.github.Bucket1572.shuffle.plugin.tag.makeHelper
import io.github.monun.tap.effect.playFirework
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
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.*
import kotlin.random.Random

class BlockShuffle(private val plugin: MinigameShufflePlugin):
    Minigame(
        plugin.server, "블록 셔플", "제시되는 블록에 가장 먼저 올라가세요.",
        Material.GRASS_BLOCK,
        listOf(
            "제시되는 블록을 찾고 가장 먼저 올라가야 합니다."
        ),
        listOf(
            "제한 시간 내에 블록 위에 올라가지 못했을 경우"
        )
    ), Listener
{
    private var targetBlock = ItemStack(Material.GRASS)
    private val blockFinderRanking = mutableListOf<Player>()

    override fun getHelperTools(): List<ItemStack> {
        return emptyList()
    }

    override fun additionalPreparation() {
        resetTarget()
        blockFinderRanking.clear()
        broadcastTarget()
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    override fun judge(player: Player, rankings: List<Player>): MinigameResult {
        if (player !in blockFinderRanking) return MinigameResult.FAIL
        if (player == rankings[0]) return MinigameResult.WIN
        return MinigameResult.LOSE
    }

    override fun getRankings(): List<Player> {
        return blockFinderRanking
    }

    override fun additionalCleanUp() {
        InventoryClickEvent.getHandlerList().unregister(this)
    }

    override fun getRewards(): List<ItemStack> {
        return emptyList()
    }

    private fun resetTarget() {
        targetBlock = ItemStack(getBlock())
    }

    private fun broadcastTarget() {
        plugin.server.broadcast(
            Component.text("✔ 찾아야 할 블록").color(ColorTag.MINIGAME_DESCRIPTION.getTextColor())
        )
        plugin.server.broadcast(
            (targetBlock.displayName() as TranslatableComponent)
                .hoverEvent(targetBlock.asHoverEvent())
        )
    }

    private fun getBlock(): Material {
        return getAllBlock().random()
    }

    private fun getAllBlock(): List<Material> {
        return Material.values().filter {
            it.isBlock
        }
    }

    @EventHandler
    fun onSteppingOnTarget(event: PlayerMoveEvent) {
        if (!isTargetAchieved(event)) return

        blockFinderRanking.add(event.player)
        val location = event.player.location
        location.world.spawn(location, Firework::class.java).apply {
            fireworkMeta = fireworkMeta.also { meta ->
                meta.addEffect(
                    FireworkEffect.builder().with(FireworkEffect.Type.STAR)
                        .withColor(Color.LIME, Color.GREEN, Color.YELLOW)
                        .build()
                )
                meta.power = 0
            }
        }
    }

    private fun isTargetAchieved(event: PlayerMoveEvent): Boolean {
        val location = event.player.location
        return (location.subtract(0.0, 0.3, 0.0).block.type == targetBlock.type)
                && (event.player !in blockFinderRanking)
    }
}