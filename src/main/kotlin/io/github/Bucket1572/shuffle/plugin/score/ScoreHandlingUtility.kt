package io.github.Bucket1572.shuffle.plugin.score

import io.github.Bucket1572.shuffle.plugin.tag.ColorTag
import io.github.Bucket1572.shuffle.plugin.tag.getTextColor
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Scoreboard

object ScoreHandlingUtility {
    private const val SCORE_NAME = "점수"

    var scoreboard: Scoreboard? = null

    const val score = 1

    fun initScoreBoard(server: Server) {
        val board = Bukkit.getScoreboardManager().newScoreboard
        val objective = board.registerNewObjective(
            SCORE_NAME, "dummy",
            Component.text(SCORE_NAME, ColorTag.SCOREBOARD.getTextColor())
        )
        objective.displaySlot = DisplaySlot.SIDEBAR
        scoreboard = board

        server.onlinePlayers.forEach {
            initScore(it)
        }
    }

    private fun initScore(player: Player) {
        setScore(player, 0)
        player.scoreboard = scoreboard!!
    }

    fun updateScore(player: Player, amount: Int) {
        val scoreObjective = scoreboard!!.getObjective(SCORE_NAME)
        val score = scoreObjective!!.getScore(player.name)
        score.score += amount
    }

    private fun setScore(player: Player, score: Int) {
        val scoreObjective = scoreboard!!.getObjective(SCORE_NAME)
        scoreObjective!!.getScore(player.name).score = score
    }
}

fun Player.win(amount: Int) {
    ScoreHandlingUtility.updateScore(this, amount)
}

fun Player.win() {
    this.win(ScoreHandlingUtility.score)
}

fun Player.fail(amount: Int) {
    ScoreHandlingUtility.updateScore(this, -amount)
}

fun Player.fail() {
    this.fail(ScoreHandlingUtility.score)
}