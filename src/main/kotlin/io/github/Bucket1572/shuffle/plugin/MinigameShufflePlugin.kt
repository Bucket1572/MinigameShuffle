package io.github.Bucket1572.shuffle.plugin

import io.github.Bucket1572.shuffle.plugin.minigame.*
import io.github.Bucket1572.shuffle.plugin.score.ScoreHandlingUtility
import io.github.monun.kommand.kommand
import org.bukkit.World
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class MinigameShufflePlugin: JavaPlugin() {
    val playAreaLowerLeft = mutableMapOf(Pair("x", 0.0), Pair("z", 0.0))
    val playAreaUpperRight = mutableMapOf(Pair("x", 0.0), Pair("z", 0.0))
    var playAreaWorld: World? = null

    override fun onEnable() {
        registerCommands()
    }

    private fun registerCommands() = kommand {
        register("test") {
            then(
                "minigame" to dynamicByEnum(
                    EnumSet.allOf(MinigameTag::class.java)
                )
            ) {
                executes { context ->
                    val worldBorder = location.world.worldBorder
                    worldBorder.size = 100.0
                    worldBorder.center = location.toBlockLocation()

                    playAreaLowerLeft["x"] = worldBorder.center.x - 50.0
                    playAreaLowerLeft["z"] = worldBorder.center.z - 50.0

                    playAreaUpperRight["x"] = worldBorder.center.x + 50.0
                    playAreaUpperRight["z"] = worldBorder.center.z + 50.0

                    playAreaWorld = location.world

                    val minigameTag = context.get<MinigameTag>("minigame")
                    testMinigame(minigameTag)
                }
            }
        }
    }

    private fun testMinigame(minigameTag: MinigameTag) {
        ScoreHandlingUtility.initScoreBoard(server)
        val minigame = when (minigameTag) {
            MinigameTag.GEMS -> SplendidGems(this)
            MinigameTag.CLIMBING -> ClimbingContest(this)
            MinigameTag.PUPPY -> CutePuppy(this)
            MinigameTag.CAT -> CatServant(this)
            MinigameTag.HIDE_AND_SEEK -> HideAndSeek(this)
            MinigameTag.DELIVERY -> OcelotDelivery(this)
            MinigameTag.ALCHEMY -> IntroductionToPotions(this)
        }
        minigame.start()
    }
}