package io.github.Bucket1572.shuffle.plugin.minigame

import io.github.Bucket1572.shuffle.plugin.MinigameShufflePlugin
import io.github.Bucket1572.shuffle.plugin.common.any
import io.github.Bucket1572.shuffle.plugin.common.count
import io.github.Bucket1572.shuffle.plugin.result.MinigameResult
import io.github.Bucket1572.shuffle.plugin.tag.ColorTag
import io.github.Bucket1572.shuffle.plugin.tag.getTextColor
import io.github.Bucket1572.shuffle.plugin.tag.makeHelper
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryPickupItemEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.*
import kotlin.random.Random

class IntroductionToAlchemy(private val plugin: MinigameShufflePlugin):
    Minigame(
        plugin.server, "포션학개론", "제시되는 효과의 포션을 가장 먼저 양조하세요.",
        Material.BLAZE_POWDER,
        listOf(
            "제시되는 효과의 포션을 가장 먼저 양조하여야 합니다."
        ),
        listOf(
            "제한 시간 내에 포션을 양조하지 못했을 경우"
        )
    ), Listener
{
    private var targetPotionMaterial = Material.POTION
    private var targetPotion = ItemStack(targetPotionMaterial, 1)
    private val potionRanking = mutableListOf<Player>()

    override fun getHelperTools(): List<ItemStack> {
        val brewingStand = ItemStack(Material.BREWING_STAND, 1)
        val blazeRod = ItemStack(Material.BLAZE_ROD, 10)
        val dragonBreath = ItemStack(Material.DRAGON_BREATH, 1)
        brewingStand.makeHelper(this)
        blazeRod.makeHelper(this)
        dragonBreath.makeHelper(this)
        return listOf(brewingStand, blazeRod, dragonBreath)
    }

    override fun additionalPreparation() {
        resetTarget()
        potionRanking.clear()
        broadcastTarget()
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    override fun judge(player: Player, rankings: List<Player>): MinigameResult {
        if (player !in potionRanking) return MinigameResult.FAIL
        if (player == rankings[0]) return MinigameResult.WIN
        return MinigameResult.LOSE
    }

    override fun getRankings(): List<Player> {
        return potionRanking
    }

    override fun additionalCleanUp() {
        InventoryClickEvent.getHandlerList().unregister(this)
    }

    override fun getRewards(): List<ItemStack> {
        return emptyList()
    }

    private fun resetTarget() {
        targetPotionMaterial = listOf(Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION).random()
        val targetPotionType = PotionType.values().filterNot {
            (it == PotionType.UNCRAFTABLE)
                    || (it == PotionType.LUCK)
        }.random()

        val potionData = targetPotionType.let { potionType ->
            if (potionType.isExtendable) {
                if (potionType.isUpgradeable) {
                    val extendAndUpgrade = listOf(
                        Pair(first = true, second = false),
                        Pair(first = false, second = true),
                        Pair(first = false, second = false)
                    ).random()
                    return@let PotionData(potionType, extendAndUpgrade.first, extendAndUpgrade.second)
                }
            }

            return@let PotionData(potionType)
        }
        targetPotion = ItemStack(targetPotionMaterial, 1)
        targetPotion.editMeta {
            val potionMeta = it as PotionMeta
            potionMeta.basePotionData = potionData
        }
    }

    private fun broadcastTarget() {
        plugin.server.broadcast(
            Component.text("✔ 양조해야 할 포션").color(ColorTag.MINIGAME_DESCRIPTION.getTextColor())
        )
        plugin.server.broadcast(
            (targetPotion.displayName() as TranslatableComponent)
                .hoverEvent(targetPotion.asHoverEvent())
        )
    }

    @EventHandler
    fun onBrewingTarget(event: InventoryClickEvent) {
        if (!isTargetBrewed(event)) return

        potionRanking.add(event.view.player as Player)
    }

    private fun isTargetBrewed(event: InventoryClickEvent): Boolean {
        return (event.inventory.type == InventoryType.BREWING)
                && (event.currentItem == targetPotion)
                && (event.view.player !in potionRanking)
    }
}