package io.github.Bucket1572.shuffle.plugin.minigame

import io.github.Bucket1572.shuffle.plugin.MinigameShufflePlugin
import io.github.Bucket1572.shuffle.plugin.common.any
import io.github.Bucket1572.shuffle.plugin.common.count
import io.github.Bucket1572.shuffle.plugin.result.MinigameResult
import io.github.Bucket1572.shuffle.plugin.tag.ColorTag
import io.github.Bucket1572.shuffle.plugin.tag.getTextColor
import io.github.Bucket1572.shuffle.plugin.tag.isHelper
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
import org.bukkit.event.player.PlayerItemBreakEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.*
import kotlin.random.Random

class SeriousDedicationProject(private val plugin: MinigameShufflePlugin):
    Minigame(
        plugin.server, "도를 넘은 전념", "제공되는 아이템의 내구도를 전부 사용하세요.",
        Material.WITHER_ROSE,
        listOf(
            "제공되는 아이템을 빨리 사용해야 합니다."
        ),
        listOf(
            "제한 시간 아이템을 사용하지 못했을 경우"
        )
    ), Listener
{
    private var targetItem = ItemStack(Material.NETHERITE_HOE)
    private val dedicationRanking = mutableListOf<Player>()

    override fun getHelperTools(): List<ItemStack> {
        resetTarget()
        return listOf(targetItem)
    }

    override fun additionalPreparation() {
        dedicationRanking.clear()
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    override fun judge(player: Player, rankings: List<Player>): MinigameResult {
        if (player !in dedicationRanking) return MinigameResult.FAIL
        if (player == rankings[0]) return MinigameResult.WIN
        return MinigameResult.LOSE
    }

    override fun getRankings(): List<Player> {
        return dedicationRanking
    }

    override fun additionalCleanUp() {
        PlayerItemBreakEvent.getHandlerList().unregister(this)
    }

    override fun getRewards(): List<ItemStack> {
        return emptyList()
    }

    private fun resetTarget() {
        targetItem = ItemStack(getItem())
        val durableFlag = Random.nextDouble()
        if (durableFlag < 0.9) {
            targetItem.addEnchantment(
                Enchantment.DURABILITY,
                Random.nextInt(1, 3)
            )
        }
        targetItem.makeHelper(this)
    }

    private fun getItem(): Material {
        return getAllItem().random()
    }

    private fun getAllItem(): List<Material> {
        return Material.values().filter {
            it.maxDurability > 0
        }
    }

    @EventHandler
    fun onDedicationEnd(event: PlayerItemBreakEvent) {
        if (!isDedication(event)) return

        dedicationRanking.add(event.player)
    }

    private fun isDedication(event: PlayerItemBreakEvent): Boolean {
        return (event.brokenItem.type == targetItem.type)
                && (event.brokenItem.isHelper(this))
                && (event.player !in dedicationRanking)
    }
}