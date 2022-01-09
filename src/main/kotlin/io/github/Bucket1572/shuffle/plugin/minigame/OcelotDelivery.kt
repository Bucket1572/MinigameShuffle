package io.github.Bucket1572.shuffle.plugin.minigame

import io.github.Bucket1572.shuffle.plugin.MinigameShufflePlugin
import io.github.Bucket1572.shuffle.plugin.result.MinigameResult
import io.github.Bucket1572.shuffle.plugin.tag.ColorTag
import io.github.Bucket1572.shuffle.plugin.tag.getTextColor
import io.papermc.paper.event.player.PlayerTradeEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Villager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Merchant
import org.bukkit.inventory.MerchantRecipe
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.random.Random

class OcelotDelivery(private val plugin: MinigameShufflePlugin):
    Minigame(
        plugin.server, "오실롯 배달", "가장 빨리 주문을 완료하세요.",
        Material.LEATHER_HORSE_ARMOR,
        listOf(
            "가장 빨리 손님이 주문한 음식을 만들어 손님에게 배달해야 합니다.",
            "손님은 게임 영역 내 임의의 위치에 나타납니다.",
            "손님의 주문은 모두에게 공지됩니다.",
            "나침반을 우클릭하여 손님의 위치를 추적할 수 있습니다."
        ),
        listOf(
            "제한 시간 내에 손님에게 음식을 배달하지 못한 경우"
        )
    ), Listener
{
    private val tip = ItemStack(Material.EMERALD)
    private var customerLocation = getCustomerLocation()
    private var customer: Villager? = null
    private val orderRanking = mutableListOf<Player>()

    override fun getHelperTools(): List<ItemStack> {
        return emptyList()
    }

    override fun additionalPreparation() {
        resetTip()

        val order = receiveOrder()
        customer = settingUpCustomer(order)
        broadcastOrder(order)

        plugin.server.pluginManager.registerEvents(this, plugin)

        orderRanking.clear()
    }

    override fun getRewards(): List<ItemStack> {
        return listOf(ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 2))
    }

    override fun judge(player: Player, rankings: List<Player>): MinigameResult {
        if (player !in orderRanking) return MinigameResult.FAIL
        if (player == orderRanking[0]) return MinigameResult.WIN
        return MinigameResult.LOSE
    }

    override fun getRankings(): List<Player> {
        return orderRanking
    }

    override fun additionalCleanUp() {
        PlayerTradeEvent.getHandlerList().unregister(this)
        PlayerInteractEvent.getHandlerList().unregister(this)

        customer?.remove()
    }

    private fun resetTip() {
        tip.editMeta {
            it.displayName(
                Component.text("배달 팁").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                    .color(ColorTag.REWARD.getTextColor())
            )
        }
    }

    private fun settingUpCustomer(order: MerchantRecipe): Villager {
        customerLocation = getCustomerLocation()
        val customer = plugin.playAreaWorld!!.spawnEntity(customerLocation, EntityType.VILLAGER) as Villager
        customer.profession = Villager.Profession.NITWIT
        customer.addPotionEffect(
            PotionEffect(
                PotionEffectType.GLOWING, minigameLength * 20, 0,
                true, false, true
            )
        )
        customer.addPotionEffect(
            PotionEffect(
                PotionEffectType.SLOW, minigameLength * 20, 10,
                true, false, true
            )
        )
        customer.addPotionEffect(
            PotionEffect(
                PotionEffectType.DAMAGE_RESISTANCE, minigameLength * 20, 10,
                true, false, true
            )
        )

        val customerMerchant = customer as Merchant
        customerMerchant.recipes = listOf(order)

        return customer
    }

    private fun getCustomerLocation(): Location {
        val customerLocationMap =
            mapOf(
                Pair("x", Random.nextDouble(plugin.playAreaLowerLeft["x"]!!, plugin.playAreaUpperRight["x"]!!)),
                Pair("z", Random.nextDouble(plugin.playAreaLowerLeft["z"]!!, plugin.playAreaUpperRight["z"]!!))
            )
        val customerLocationY = plugin.playAreaWorld!!.getHighestBlockYAt(
            customerLocationMap["x"]!!.toInt(),
            customerLocationMap["z"]!!.toInt()
        ) + 1
        return Location(
            plugin.playAreaWorld, customerLocationMap["x"]!!, customerLocationY.toDouble(), customerLocationMap["z"]!!
        )
    }

    private fun receiveOrder(): MerchantRecipe {
        val order = MerchantRecipe(
            tip, plugin.server.onlinePlayers.size + 5
        )
        val firstMenu = getFood()
        var secondMenu = getFood()
        while (firstMenu == secondMenu) secondMenu = getFood()

        order.ingredients = listOf(
            ItemStack(firstMenu, 1),
            ItemStack(secondMenu, 1)
        )

        return order
    }

    private fun broadcastOrder(order: MerchantRecipe) {
        plugin.server.broadcast(
            Component.text("✔ 손님의 주문").color(ColorTag.MINIGAME_DESCRIPTION.getTextColor())
        )
        plugin.server.broadcast(
            (order.ingredients[0].displayName() as TranslatableComponent)
                .color(ColorTag.MINIGAME_DESCRIPTION.getTextColor())
                .append(Component.text(" "))
                .append(
                    order.ingredients[1].displayName() as TranslatableComponent
                ).color(ColorTag.MINIGAME_DESCRIPTION.getTextColor())
        )
    }

    private fun getFood(): Material {
        return getAllFood().random()
    }

    private fun getAllFood(): List<Material> {
        return Material.values().filter {
            it.isEdible
        }
    }

    @EventHandler
    fun orderSucceed(event: PlayerTradeEvent) {
        if (!isDeliveryEvent(event)) return

        orderRanking.add(event.player)
    }

    @EventHandler
    fun trackCustomer(event: PlayerInteractEvent) {
        if (!isTrackingEvent(event)) return

        event.player.compassTarget = customerLocation
        event.player.setCooldown(Material.COMPASS, 60)
    }

    private fun isDeliveryEvent(event: PlayerTradeEvent): Boolean {
        return (event.trade.result == tip)
                && (event.player !in orderRanking)
    }

    private fun isTrackingEvent(event: PlayerInteractEvent): Boolean {
        return (event.item?.type == Material.COMPASS)
                && (!event.player.hasCooldown(Material.COMPASS))
                && ((event.action == Action.RIGHT_CLICK_BLOCK) || (event.action == Action.RIGHT_CLICK_AIR))
    }
}