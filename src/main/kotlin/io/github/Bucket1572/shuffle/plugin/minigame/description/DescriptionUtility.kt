package io.github.Bucket1572.shuffle.plugin.minigame.description

import io.github.Bucket1572.shuffle.plugin.tag.ColorTag
import io.github.Bucket1572.shuffle.plugin.tag.getTextColor
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent
import org.bukkit.inventory.ItemStack

object DescriptionUtility {
    fun generateDescription(
        title: Component, details: List<String>, failCondition: List<String>, rewards: List<ItemStack>
    ): Component {
        return Component.text()
            .append(getCase()).newline()
            .append(getTitle(title)).newline()
            .append(getDetails(details)).newline()
            .newline()
            .append(Component.text("⚠ 실격 조건").color(ColorTag.ALERT.getTextColor())).newline()
            .append(getFailCondition(failCondition)).newline()
            .newline()
            .append(Component.text("✔ 추가 보상").color(ColorTag.REWARD.getTextColor())).newline()
            .append(getDetailedExplanationOfItem(rewards)).newline()
            .append(getCase())
            .build()
    }

    private fun getCase(): Component {
        return Component.text("==================================================")
            .color(ColorTag.BUFFER.getTextColor())
    }

    private fun getTitle(title: Component): Component {
        return title.color(ColorTag.MINIGAME_TITLE.getTextColor())
    }

    private fun getDetails(details: List<String>): List<Component> {
        return details.map { Component.text(it) }.mapIndexed { index, detail ->
            if (index < details.size - 1) detail.newline()
            else detail
        }
    }

    private fun getFailCondition(failCondition: List<String>): List<Component> {
        return failCondition.map { Component.text(it) }.map { condition ->
            Component.text("* ").append(condition).color(ColorTag.ALERT.getTextColor())
        }
    }

    private fun getDetailedExplanationOfItem(itemStacks: List<ItemStack>): List<Component> {
        val hoverComponent = itemStacks.map { itemStack ->
            val translatableComponent = itemStack.displayName() as TranslatableComponent
            val itemName = translatableComponent.asComponent()
                .color(ColorTag.REWARD.getTextColor())
            Component.text("* ").append(itemName)
                .hoverEvent(itemStack.asHoverEvent()).color(ColorTag.REWARD.getTextColor())
        }
        return hoverComponent
    }
}

fun TextComponent.Builder.newline(): TextComponent.Builder {
    return this.append(Component.text("\n"))
}

fun Component.newline(): Component {
    return this.append(Component.text("\n"))
}