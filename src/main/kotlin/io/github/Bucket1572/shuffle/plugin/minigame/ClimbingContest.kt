package io.github.Bucket1572.shuffle.plugin.minigame

import io.github.Bucket1572.shuffle.plugin.minigame.description.DescriptionUtility
import io.github.Bucket1572.shuffle.plugin.result.MinigameResult
import io.github.Bucket1572.shuffle.plugin.tag.ColorTag
import io.github.Bucket1572.shuffle.plugin.tag.getTextColor
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.math.abs

class ClimbingContest(private val server: Server) :
    Minigame(
        server, "높이높이 올라올라", "가장 높은 위치에 도달하세요.",
        Material.FIREWORK_ROCKET,
        listOf(
            "제한 시간 안에 가장 높은 위치에 도달해야 합니다.",
            "제한 시간이 끝나면, 플레이어의 높이를 계산합니다.",
            "그 결과 플레이어의 위치의 y값이 가장 큰 플레이어가 승리합니다."
        ),
        listOf(
            "제한 시간 이후 y값이 0보다 작을 경우"
        )
    ) {

    override fun getHelperTools(): List<ItemStack> {
        return listOf()
    }

    override fun getRewards(): List<ItemStack> {
        val potion = ItemStack(Material.POTION, 1)
        potion.editMeta {
            val potionMeta = it as PotionMeta
            potionMeta.addCustomEffect(
                PotionEffect(
                    PotionEffectType.DAMAGE_RESISTANCE, 3 * 60 * 20, 1,
                    true, false, true
                ),
                true
            )
            potionMeta.addCustomEffect(
                PotionEffect(
                    PotionEffectType.SLOW_FALLING, 1 * 60 * 20, 0,
                    true, false, true
                ),
                true
            )
            potionMeta.color = Color.LIME
            potionMeta.displayName(
                Component.text("산의 정기")
                    .color(ColorTag.REWARD.getTextColor())
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
            )
        }
        return listOf(potion)
    }

    override fun judge(player: Player, rankings: List<Player>): MinigameResult {
        val playerLowerThanZeroFlag = getHeight(player) < 0
        if (playerLowerThanZeroFlag) return MinigameResult.FAIL

        val playerHeight = getHeight(player)
        val firstHeight = getHeight(rankings[0])
        if (abs(playerHeight - firstHeight) < 1e-5) return MinigameResult.WIN

        return MinigameResult.LOSE
    }

    override fun getRankings(): List<Player> {
        return server.onlinePlayers.sortedByDescending {
            getHeight(it)
        }
    }

    private fun getHeight(player: Player): Double {
        return player.location.y
    }
}