package io.github.Bucket1572.shuffle.plugin.score

import io.github.Bucket1572.shuffle.plugin.color.ColorTag
import io.github.Bucket1572.shuffle.plugin.color.getTextColor
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Scoreboard

object ScoreHandlingUtility {
    private const val SCORE_NAME = "점수"

    var scoreboard: Scoreboard? = null

    fun initScoreBoard() {
        val board = Bukkit.getScoreboardManager().newScoreboard
        val objective = board.registerNewObjective(
            SCORE_NAME, "dummy",
            Component.text(SCORE_NAME, ColorTag.SCOREBOARD.getTextColor())
        )
        objective.displaySlot = DisplaySlot.SIDEBAR
        scoreboard = board
    }

    fun initScore(player: Player) {
        setScore(player, 0)
        player.scoreboard = scoreboard!!
    }

    fun updateScore(player: Player, amount: Int) {
        val scoreObjective = scoreboard!!.getObjective(SCORE_NAME)
        val score = scoreObjective!!.getScore(player.name)
        score.score += amount
    }

    fun setScore(player: Player, score: Int) {
        val scoreObjective = scoreboard!!.getObjective(SCORE_NAME)
        scoreObjective!!.getScore(player.name).score = score
    }
}