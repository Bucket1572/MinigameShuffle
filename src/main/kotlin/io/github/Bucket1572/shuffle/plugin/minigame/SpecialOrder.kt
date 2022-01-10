package io.github.Bucket1572.shuffle.plugin.minigame

import io.github.Bucket1572.shuffle.plugin.MinigameShufflePlugin
import io.github.Bucket1572.shuffle.plugin.result.MinigameResult
import io.github.Bucket1572.shuffle.plugin.tag.ColorTag
import io.github.Bucket1572.shuffle.plugin.tag.getTextColor
import io.papermc.paper.event.player.PlayerTradeEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Villager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.*
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.random.Random

class SpecialOrder(private val plugin: MinigameShufflePlugin):
    Minigame(
        plugin.server, "스페셜 오더", "제시되는 특별한 주문을 완료하세요.",
        Material.RABBIT_STEW,
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

    private val specialDishRecipeName = "specialdish"

    override fun getHelperTools(): List<ItemStack> {
        return emptyList()
    }

    override fun additionalPreparation() {
        resetTip()

        val order = receiveOrder()
        customer = settingUpCustomer(order)
        broadcastOrder(order)
        broadcastRecipe()

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

        plugin.server.onlinePlayers.forEach {
            it.undiscoverRecipe(NamespacedKey.fromString(specialDishRecipeName)!!)
        }

        Bukkit.removeRecipe(NamespacedKey.fromString(specialDishRecipeName)!!)

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
        val specialMenu = makeDish()

        order.ingredients = listOf(
            specialMenu
        )

        return order
    }

    private fun makeDish(): ItemStack {
        val baseIngredient = listOf(
            Material.MUSHROOM_STEW, Material.RABBIT_STEW,
            Material.PUMPKIN_PIE, Material.COOKED_BEEF, Material.COOKED_CHICKEN,
            Material.COOKED_MUTTON, Material.COOKED_PORKCHOP,
            Material.CAKE, Material.COOKIE, Material.COOKED_COD, Material.COOKED_SALMON
        ).random()
        return when (baseIngredient) {
            Material.MUSHROOM_STEW, Material.RABBIT_STEW -> specialStew(baseIngredient)
            Material.PUMPKIN_PIE, Material.CAKE, Material.COOKIE -> specialDessert(baseIngredient)
            Material.COOKED_BEEF, Material.COOKED_CHICKEN, Material.COOKED_MUTTON, Material.COOKED_PORKCHOP,
            Material.COOKED_COD, Material.COOKED_SALMON
                -> specialSteak(baseIngredient)
            else -> ItemStack(Material.AIR)
        }
    }

    private fun specialStew(baseIngredient: Material): ItemStack {
        val vegetable = listOf(Material.BEETROOT, Material.CARROT, Material.BAKED_POTATO).random()
        val creamFlag = Random.nextBoolean()
        val dishName = baseIngredient.let {
            val vegetablePrefix = when(vegetable) {
                Material.BEETROOT -> "사탕무를 넣은 "
                Material.CARROT -> "당근을 넣은 "
                Material.BAKED_POTATO -> "감자를 넣은 "
                else -> ""
            }
            val creamPrefix = if (creamFlag) "크리미한 " else ""
            return@let vegetablePrefix + creamPrefix + when(it) {
                Material.MUSHROOM_STEW -> "버섯 스튜"
                Material.RABBIT_STEW -> "토끼 스튜"
                else -> ""
            }
        }
        val specialDish = ItemStack(baseIngredient)
        specialDish.editMeta {
            it.displayName(
                Component.text(dishName).color(TextColor.color(255, 255, 255))
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
            )
        }

        if (creamFlag) {
            val recipe = ShapelessRecipe(NamespacedKey.fromString(specialDishRecipeName)!!, specialDish)
            recipe.addIngredient(vegetable)
            recipe.addIngredient(Material.MILK_BUCKET)
            recipe.addIngredient(baseIngredient)
            Bukkit.addRecipe(recipe)

            return specialDish
        }

        val recipe = ShapelessRecipe(NamespacedKey.fromString(specialDishRecipeName)!!, specialDish)
        recipe.addIngredient(vegetable)
        recipe.addIngredient(baseIngredient)
        Bukkit.addRecipe(recipe)

        return specialDish
    }

    private fun specialDessert(baseIngredient: Material): ItemStack {
        if (baseIngredient == Material.PUMPKIN_PIE) {
            val meatPieFlag = Random.nextBoolean()
            if (meatPieFlag)
                return meatPie()
        }
        if (baseIngredient == Material.CAKE) {
            val carrotCakeFlag = Random.nextBoolean()
            if (carrotCakeFlag) {
                return carrotCake()
            }
        }
        val berry = listOf(Material.GLOW_BERRIES, Material.SWEET_BERRIES, Material.APPLE, Material.GOLDEN_APPLE).random()
        val honeyFlag = Random.nextBoolean()
        val goldFlag = Random.nextBoolean()
        val seedFlag = Random.nextBoolean()
        val chocoFlag = Random.nextBoolean()

        val berryPrefix = when (berry) {
            Material.GLOW_BERRIES -> "발광 열매로 장식한 "
            Material.SWEET_BERRIES -> "달콤한 열매로 장식한 "
            Material.APPLE -> "사과로 마무리한 "
            Material.GOLDEN_APPLE -> "황금 사과로 마무리한 "
            else -> ""
        }

        val honeyPrefix = if (honeyFlag) "꿀이 들어간 " else ""
        val goldPrefix = if (goldFlag) "금박을 입힌 " else ""
        val seedPrefix = if (seedFlag) "오곡 " else ""
        val chocoPrefix = if (chocoFlag) "초코 " else ""

        val baseDessertName = when (baseIngredient) {
            Material.PUMPKIN_PIE -> "호박 파이"
            Material.CAKE -> "케이크"
            Material.COOKIE -> "쿠키"
            else -> ""
        }

        val dishName = berryPrefix + honeyPrefix + goldPrefix + seedPrefix + chocoPrefix + baseDessertName
        val specialDish = ItemStack(baseIngredient)
        specialDish.editMeta {
            it.displayName(
                Component.text(dishName).color(TextColor.color(255, 255, 255))
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
            )
        }

        val recipe = ShapelessRecipe(NamespacedKey.fromString(specialDishRecipeName)!!, specialDish)
        recipe.addIngredient(berry)
        if (honeyFlag) recipe.addIngredient(Material.HONEY_BOTTLE)
        if (goldFlag) recipe.addIngredient(Material.GOLD_NUGGET)
        if (seedFlag) recipe.addIngredient(Material.WHEAT_SEEDS)
        if (chocoFlag) recipe.addIngredient(Material.COCOA_BEANS)
        Bukkit.addRecipe(recipe)

        return specialDish
    }

    private fun meatPie(): ItemStack {
        val meat = listOf(
            Material.COOKED_PORKCHOP, Material.COOKED_MUTTON, Material.COOKED_CHICKEN,
            Material.COOKED_BEEF, Material.COOKED_RABBIT
        ).random()
        val vegetable = listOf(
            Material.POTATO, Material.CARROT
        ).random()

        val vegetablePrefix = when (vegetable) {
            Material.POTATO -> "감자를 넣은 "
            Material.CARROT -> "당근을 넣은 "
            else -> ""
        }

        val meatBaseName = when (meat) {
            Material.COOKED_PORKCHOP -> "돼지고기 미트 파이"
            Material.COOKED_MUTTON -> "양고기 미트 파이"
            Material.COOKED_CHICKEN -> "닭고기 미트 파이"
            Material.COOKED_BEEF -> "소고기 미트 파이"
            Material.COOKED_RABBIT -> "토끼고기 미트 파이"
            else -> ""
        }

        val dishName = vegetablePrefix + meatBaseName
        val specialDish = ItemStack(Material.PUMPKIN_PIE)
        specialDish.editMeta {
            it.displayName(
                Component.text(dishName).color(TextColor.color(255, 255, 255))
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
            )
        }

        val recipe = ShapelessRecipe(NamespacedKey.fromString(specialDishRecipeName)!!, specialDish)
        recipe.addIngredient(vegetable)
        recipe.addIngredient(meat)
        recipe.addIngredient(Material.EGG)
        recipe.addIngredient(Material.SUGAR)
        Bukkit.addRecipe(recipe)

        return specialDish
    }

    private fun carrotCake(): ItemStack {
        val carrotCake = ItemStack(Material.CAKE)
        carrotCake.editMeta {
            it.displayName(
                Component.text("당근 케이크").color(TextColor.color(255, 255, 255))
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
            )
        }
        val carrotCakeRecipe = ShapedRecipe(NamespacedKey.fromString(specialDishRecipeName)!!, carrotCake)
        carrotCakeRecipe.setIngredient('M', Material.MILK_BUCKET)
        carrotCakeRecipe.setIngredient('E', Material.EGG)
        carrotCakeRecipe.setIngredient('W', Material.WHEAT)
        carrotCakeRecipe.setIngredient('C', Material.CARROT)
        carrotCakeRecipe.setIngredient('S', Material.SUGAR)

        carrotCakeRecipe.shape("MMM", "SES", "WCW")
        Bukkit.addRecipe(carrotCakeRecipe)

        return carrotCake
    }

    private fun specialSteak(baseIngredient: Material): ItemStack {
        val sideDish = listOf(Material.BAKED_POTATO, Material.SWEET_BERRIES, Material.GLOW_BERRIES).random()
        val sideDishPrefix = when (sideDish) {
            Material.BAKED_POTATO -> "매쉬드 포테이토를 곁들인 "
            Material.SWEET_BERRIES -> "달콤한 열매를 곁들인 "
            Material.GLOW_BERRIES -> "발광 열매를 곁들인 "
            else -> ""
        }

        val baseSteakName = when (baseIngredient) {
            Material.COOKED_BEEF -> "소고기 스테이크"
            Material.COOKED_CHICKEN -> "치킨 스테이크"
            Material.COOKED_MUTTON -> "양고기 스테이크"
            Material.COOKED_PORKCHOP -> "폭찹"
            Material.COOKED_COD -> "생선 구이"
            Material.COOKED_SALMON -> "연어 스테이크"
            else -> ""
        }

        val dishName = sideDishPrefix + baseSteakName

        val specialDish = ItemStack(baseIngredient)
        specialDish.editMeta {
            it.displayName(
                Component.text(dishName).color(TextColor.color(255, 255 ,255))
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
            )
        }

        val recipe = ShapelessRecipe(NamespacedKey.fromString(specialDishRecipeName)!!, specialDish)
        recipe.addIngredient(sideDish)
        recipe.addIngredient(baseIngredient)
        Bukkit.addRecipe(recipe)

        return specialDish
    }

    private fun broadcastOrder(order: MerchantRecipe) {
        plugin.server.broadcast(
            Component.text("✔ 손님의 주문").color(ColorTag.MINIGAME_DESCRIPTION.getTextColor())
        )
        plugin.server.broadcast(
            (order.ingredients[0].displayName() as TranslatableComponent)
                .color(ColorTag.MINIGAME_DESCRIPTION.getTextColor())
        )
    }

    private fun broadcastRecipe() {
        plugin.server.onlinePlayers.forEach {
            println(it.hasDiscoveredRecipe(NamespacedKey.fromString(specialDishRecipeName)!!))
            it.discoverRecipe(NamespacedKey.fromString(specialDishRecipeName)!!)
            println(it.hasDiscoveredRecipe(NamespacedKey.fromString(specialDishRecipeName)!!))
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