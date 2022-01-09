package io.github.Bucket1572.shuffle.plugin.minigame

import io.github.Bucket1572.shuffle.plugin.MinigameShufflePlugin
import io.github.Bucket1572.shuffle.plugin.common.any
import io.github.Bucket1572.shuffle.plugin.common.count
import io.github.Bucket1572.shuffle.plugin.result.MinigameResult
import io.github.Bucket1572.shuffle.plugin.tag.ColorTag
import io.github.Bucket1572.shuffle.plugin.tag.getTextColor
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class HideAndSeek(private val plugin: MinigameShufflePlugin):
    Minigame(
        plugin.server, "숨바꼭질", "술래에게 가장 가깝게 접근하세요. 들키지 마세요!",
        Material.TINTED_GLASS,
        listOf(
            "게임이 시작되면 술래가 한 명 정해집니다.",
            "술래는 힘 5, 저항 5, 신속 1의 버프를 얻고 발광됩니다.",
            "술래가 아닌 사람들은 최대한 술래한테 가깝게 접근해야 합니다.",
            "제한 시간이 끝났을 때 술래 주위 20칸 안에 아무도 없다면 술래가 승리합니다.",
            "술래가 승리하지 못했다면, 술래로부터 가장 가까운 술래가 아닌 플레이어가 승리합니다.",
            "나침반을 들고 우클릭하면, 술래를 추적할 수 있습니다."
        ),
        listOf(
            "제한 시간 내에 술래 주위 40칸 안에 들어가지 못했을 경우"
        )
    ), Listener
{
    private var target: Player = plugin.server.onlinePlayers.random()

    override fun getHelperTools(): List<ItemStack> {
        return emptyList()
    }

    override fun additionalPreparation() {
        target = plugin.server.onlinePlayers.random()
        plugin.server.onlinePlayers.filterNot { it == target }.forEach {
            it.sendActionBar(
                Component.text("술래는 " + target.name + "입니다.").color(ColorTag.ALERT.getTextColor())
            )
        }
        target.sendActionBar(
            Component.text("당신이 술래입니다. 도망치세요!").color(ColorTag.ALERT.getTextColor())
        )
        target.addPotionEffect(
            PotionEffect(
                PotionEffectType.INCREASE_DAMAGE, minigameLength * 20, 4,
                true, false, true
            )
        )
        target.addPotionEffect(
            PotionEffect(
                PotionEffectType.DAMAGE_RESISTANCE, minigameLength * 20, 4,
                true, false, true
            )
        )
        target.addPotionEffect(
            PotionEffect(
                PotionEffectType.SPEED, minigameLength * 20, 0,
                true, false, true
            )
        )
        target.addPotionEffect(
            PotionEffect(
                PotionEffectType.GLOWING, minigameLength * 20, 0,
                true, false, true
            )
        )
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    override fun judge(player: Player, rankings: List<Player>): MinigameResult {
        if (rankings.isEmpty()) return MinigameResult.WIN

        if (calculateDistanceToTarget(player) > 40) return MinigameResult.FAIL
        if (calculateDistanceToTarget(rankings[0]) > 20) {
            if (player == target) return MinigameResult.WIN
            return MinigameResult.LOSE
        }

        if (player == target) return MinigameResult.LOSE
        if (player == rankings[0]) return MinigameResult.WIN
        return MinigameResult.LOSE
    }

    override fun getRankings(): List<Player> {
        return plugin.server.onlinePlayers.sortedBy {
            calculateDistanceToTarget(it)
        }.filterNot { it == target }
    }

    override fun additionalCleanUp() {
        PlayerInteractEvent.getHandlerList().unregister(this)
    }

    override fun getRewards(): List<ItemStack> {
        return emptyList()
    }

    private fun calculateDistanceToTarget(player: Player): Double {
        return player.location.distance(target.location)
    }

    @EventHandler
    fun onSearchingTarget(event: PlayerInteractEvent) {
        if (!isSearchingEvent(event)) return

        event.player.compassTarget = target.location
        event.player.setCooldown(Material.COMPASS, 60)
    }

    private fun isSearchingEvent(event: PlayerInteractEvent): Boolean {
        return (event.item?.type == Material.COMPASS)
                && (!event.player.hasCooldown(Material.COMPASS))
                && ((event.action == Action.RIGHT_CLICK_BLOCK) || (event.action == Action.RIGHT_CLICK_AIR))
                && (event.player != target)
    }
}