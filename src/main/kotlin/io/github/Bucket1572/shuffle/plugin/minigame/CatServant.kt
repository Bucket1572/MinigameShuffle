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

class CutePuppy(private val plugin: MinigameShufflePlugin):
    Minigame(
        plugin.server, "귀여운 댕댕이", "가장 먼저 늑대를 길들이세요.",
        Material.LEAD,
        listOf(
            "가장 먼저 늑대를 길들여야 합니다.",
            "이전에 길들인 늑대는 인정되지 않습니다. 새로 늑대를 길들이세요."
        ),
        listOf(
            "제한 시간 내에 늑대를 한 마리도 길들이지 못했을 경우"
        )
    ), Listener
{
    private val wolfTamingRanking = mutableListOf<Player>()

    override fun getHelperTools(): List<ItemStack> {
        return emptyList()
    }

    override fun additionalPreparation() {
        wolfTamingRanking.clear()
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    override fun getRewards(): List<ItemStack> {
        return emptyList()
    }

    override fun judge(player: Player, rankings: List<Player>): MinigameResult {
        if (player !in wolfTamingRanking) return MinigameResult.FAIL
        if (player == wolfTamingRanking[0]) return MinigameResult.WIN
        return MinigameResult.LOSE
    }

    override fun getRankings(): List<Player> {
        return wolfTamingRanking
    }

    override fun additionalCleanUp() {
        EntityTameEvent.getHandlerList().unregister(this)
    }

    @EventHandler
    fun onTamingWolf(event: EntityTameEvent) {
        val tamedAnimal = event.entityType

        if (tamedAnimal != EntityType.WOLF) return
        if (!isValidToRegister(event)) return

        wolfTamingRanking.add(event.owner as Player)
    }

    private fun isValidToRegister(event: EntityTameEvent): Boolean {
        return (event.entityType == EntityType.WOLF)
                && (event.owner is Player)
                && ((event.owner as Player) !in wolfTamingRanking)
    }
}