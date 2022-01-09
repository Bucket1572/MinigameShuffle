package io.github.Bucket1572.shuffle.plugin.minigame

import io.github.Bucket1572.shuffle.plugin.MinigameShufflePlugin
import io.github.Bucket1572.shuffle.plugin.result.MinigameResult
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityTameEvent
import org.bukkit.inventory.ItemStack

class CatServant(private val plugin: MinigameShufflePlugin):
    Minigame(
        plugin.server, "집사 되기", "가장 먼저 고양이를 길들이세요.",
        Material.WARPED_FUNGUS_ON_A_STICK,
        listOf(
            "가장 먼저 고양이를 길들여야 합니다.",
            "이전에 길들인 고양이는 인정되지 않습니다. 새로 고양이를 길들이세요."
        ),
        listOf(
            "제한 시간 내에 고양이를 한 마리도 길들이지 못했을 경우"
        )
    ), Listener
{
    private val catTamingRanking = mutableListOf<Player>()

    override fun getHelperTools(): List<ItemStack> {
        return emptyList()
    }

    override fun additionalPreparation() {
        catTamingRanking.clear()
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    override fun getRewards(): List<ItemStack> {
        return emptyList()
    }

    override fun judge(player: Player, rankings: List<Player>): MinigameResult {
        if (player !in catTamingRanking) return MinigameResult.FAIL
        if (player == catTamingRanking[0]) return MinigameResult.WIN
        return MinigameResult.LOSE
    }

    override fun getRankings(): List<Player> {
        return catTamingRanking
    }

    override fun additionalCleanUp() {
        EntityTameEvent.getHandlerList().unregister(this)
    }

    @EventHandler
    fun onBecomingServant(event: EntityTameEvent) {
        if (!isValidToRegister(event)) return

        catTamingRanking.add(event.owner as Player)
    }

    private fun isValidToRegister(event: EntityTameEvent): Boolean {
        return (event.entityType == EntityType.CAT)
                && (event.owner is Player)
                && ((event.owner as Player) !in catTamingRanking)
    }
}