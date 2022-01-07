package io.github.Bucket1572.shuffle.plugin.restrictions

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

class HelperToolRestrictions: Listener {
    @EventHandler
    fun onItemInteract(event: PlayerInteractEvent) {
        TODO("플레이어가 사용한 아이템이 지금 미니게임의 아이템이 아닐 경우 금지")
    }
}