package io.github.Bucket1572.shuffle.plugin.minigame

import io.github.Bucket1572.shuffle.plugin.common.any
import io.github.Bucket1572.shuffle.plugin.common.count
import io.github.Bucket1572.shuffle.plugin.minigame.description.DescriptionUtility
import io.github.Bucket1572.shuffle.plugin.minigame.prepare.PrepareUtility
import io.github.Bucket1572.shuffle.plugin.minigame.prepare.feed
import io.github.Bucket1572.shuffle.plugin.result.MinigameResult
import io.github.Bucket1572.shuffle.plugin.score.fail
import io.github.Bucket1572.shuffle.plugin.score.win
import io.github.Bucket1572.shuffle.plugin.tag.ColorTag
import io.github.Bucket1572.shuffle.plugin.tag.getTextColor
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.Server
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class SplendidGems(private val server: Server):
    Minigame(
        server, "반짝이는 보석들", "다이아몬드, 에메랄드와 자수정을 가장 많이 모으세요."
    )
{
    override fun prepare() {
        val minigameIcon = PrepareUtility.setMinigameIcon(Material.DIAMOND_ORE, name, summary)
        val helperTools = listOf<ItemStack>()
        val shulkerPackage = PrepareUtility.shulkerPackage(minigameIcon, helperTools)
        server.feed(shulkerPackage)
    }

    override fun getDescription(): Component {
        val title = Component.text(name).style {
            it.decoration(TextDecoration.BOLD, TextDecoration.State.TRUE)
            it.color(ColorTag.MINIGAME_TITLE.getTextColor())
        }
        val details = listOf(
            "제한 시간 안에 다이아몬드, 에메랄드와 자수정 조각을 최대한 많이 모아야 합니다.",
            "제한 시간이 끝나면 각 플레이어가 가지고 있는 보석 개수를 계산합니다.",
            "그 결과, 가장 많은 보석을 가지고 있는 플레이어가 승리합니다."
        )
        val failConditions = listOf(
            "제한 시간 내에 보석을 하나도 모으지 못했을 경우"
        )
        val rewards = getRewards()
        return DescriptionUtility.generateDescription(title, details, failConditions, rewards)
    }

    override fun judge(player: Player, rankings: List<Player>): MinigameResult {
        val inventory = player.inventory
        println("First")
        val playerHasJewelFlag = inventory.any { isJewel(it) }
        println("Second")
        if (!playerHasJewelFlag) return MinigameResult.FAIL

        val playerJewelCount = countJewels(player)
        val firstJewelCount = countJewels(rankings[0])
        if (playerJewelCount == firstJewelCount) return MinigameResult.WIN

        return MinigameResult.LOSE
    }

    override fun getRankings(): List<Player> {
        return server.onlinePlayers.sortedByDescending {
            countJewels(it)
        }
    }

    override fun winResponse(player: Player) {
        player.sendActionBar(Component.text("승리!").color(ColorTag.WIN.getTextColor()))
        player.win()
    }

    override fun loseResponse(player: Player) {
        player.sendActionBar(Component.text("패배...").color(ColorTag.LOSE.getTextColor()))
    }

    override fun failResponse(player: Player) {
        player.sendActionBar(Component.text("⚠ 실격 ⚠").color(ColorTag.FAIL.getTextColor()))
        player.fail()
    }

    private fun getRewards(): List<ItemStack> {
        val diamondPickaxe = ItemStack(Material.DIAMOND_PICKAXE)
        diamondPickaxe.editMeta {
            it.addEnchant(Enchantment.LOOT_BONUS_BLOCKS, 2, false)
        }
        return listOf(
            diamondPickaxe
        )
    }

    private fun countJewels(player: Player): Int {
        return player.inventory.count {
            isJewel(it)
        }
    }

    private fun isJewel(itemStack: ItemStack?): Boolean {
        if (itemStack != null) {
            return (itemStack.type == Material.DIAMOND)
                    || (itemStack.type == Material.EMERALD)
                    || (itemStack.type == Material.AMETHYST_SHARD)
        }
        return false
    }
}