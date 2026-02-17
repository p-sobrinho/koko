package dev.koji.koko.client.ui

import com.mojang.blaze3d.systems.RenderSystem
import dev.koji.koko.common.SkillsHandler
import dev.koji.koko.common.models.SkillData
import dev.koji.koko.common.models.SkillModel
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ItemLore

class SkillsScreen : Screen(Component.literal("Skills Screen")) {

    companion object {
        // Texturas (convertido do "built-in(ui-mc:RECT)" e "built-in(ui-mc:BORDER)")
        private val BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/demo_background.png")
        private val ITEM_STACK_CACHE = mutableMapOf<ResourceLocation, ItemStack>()

        private val maxSlotsPerLine = 6
        private val slotWidth = 20
        private val slotHeight = 20
        private val initialSpacement = 15
        private val spacementPerItem = 4
        private val spacementPerLine = 4

        private fun getItemStackFromRL(resourceLocation: ResourceLocation): ItemStack {
            return ITEM_STACK_CACHE.getOrPut(resourceLocation) { ItemStack(BuiltInRegistries.ITEM.get(resourceLocation)) }
        }
    }

    private val xSize = 160
    private var ySize = 160
    private val skillSlots = mutableListOf<SkillSlot>()

    private var xPosition = 0
    private var yPosition = 0
    private lateinit var titleText: Component
    private lateinit var player: Player

    override fun init() {
        super.init()

        val mcPlayer = minecraft?.player ?: return

        this.player = mcPlayer

        val playerSkills = SkillsHandler.getSkills(player)
        val allSkills = playerSkills.getAllSkills()

        this.ySize = 45 + (40 * (allSkills.size / 6).coerceAtLeast(1))

        this.xPosition = (width - xSize) / 2
        this.yPosition = (height - ySize) / 2
        this.skillSlots.clear()

        this.titleText = Component.literal("Your Skills")

        val slotsStartY = yPosition + 24

        var i = 0

        for ((key, value) in allSkills) {
            val line = i / maxSlotsPerLine
            val collum = i % maxSlotsPerLine

            val x = xPosition + initialSpacement + (collum * (slotWidth + spacementPerItem))
            val y = slotsStartY + (line * (slotHeight + spacementPerLine))

            val skillModel = SkillsHandler.getSkillModel(player, key)

            skillSlots.add(SkillSlot(
                x, y,
                slotWidth, slotHeight,
                key, value, skillModel
            ))

            i++
        }
    }

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(graphics, mouseX, mouseY, delta)

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)

        graphics.blit(BACKGROUND_TEXTURE, xPosition, yPosition, 0f, 0f, xSize, ySize, xSize, ySize)

        graphics.drawString(
            font, titleText,
            xPosition + (xSize - font.width(titleText)) / 2,
            yPosition + font.lineHeight,
            0x404040, false
        )

        skillSlots.forEach { it.render(graphics, mouseX, mouseY) }

        skillSlots.forEach { slot ->
            if (slot.isMouseOver(mouseX, mouseY)) {
                val skillData = slot.skillData
                val skillModel = slot.skillModel

                val skillIcon = getItemStackFromRL(SkillsHandler.safeParseResource(skillModel.icon)).apply {
                    remove(DataComponents.ATTRIBUTE_MODIFIERS)

                    set(DataComponents.CUSTOM_NAME, Component.translatable(skillModel.displayName))

                    val currentLevel = SkillsHandler.getLevel(player, slot.skillLocation)
                    val maxLevel =
                        if (slot.skillData.isOverClocked) skillModel.overClockedMaxLevel else skillModel.maxLevel

                    set(DataComponents.LORE, ItemLore(listOf(
                        Component.translatable(skillModel.description).withStyle(ChatFormatting.WHITE),
                        Component.literal("Level: $currentLevel").withStyle(ChatFormatting.BLUE),
                        Component.literal("Current XP: ${skillData.xp}").withStyle(ChatFormatting.YELLOW),
                        Component.literal("Required XP: ${SkillsHandler.xpToLevelUp((currentLevel + 1).coerceAtMost(maxLevel)).toDouble()}").withStyle(ChatFormatting.GREEN),
                    )))
                }

                graphics.renderTooltip(font, skillIcon, mouseX, mouseY)
            }
        }
    }

    override fun isPauseScreen(): Boolean = false // Equivalente ao comportamento padrão

    private class SkillSlot(
        val x: Int, val y: Int,
        val width: Int, val height: Int,
        val skillLocation: ResourceLocation, val skillData: SkillData, val skillModel: SkillModel
    ) {
        private var isHovered = false

        fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int) {
            isHovered = mouseX in x..(x + width) && mouseY in y..(y + height)

            val bgColor = if (isHovered) 0x80FFFFFF.toInt() else 0x80000000.toInt()

            graphics.fill(x, y, x + width, y + height, bgColor)

            val itemX = x + (width - 16) / 2
            val itemY = y + (height - 16) / 2

            graphics.renderFakeItem(getItemStackFromRL(
                SkillsHandler.safeParseResource(skillModel.icon)
            ), itemX, itemY)

            graphics.fill(x, y, x + width, y + 1, 0xFFFFFFFF.toInt())
            graphics.fill(x, y + height - 1, x + width, y + height, 0xFFFFFFFF.toInt())
            graphics.fill(x, y, x + 1, y + height, 0xFFFFFFFF.toInt())
            graphics.fill(x + width - 1, y, x + width, y + height, 0xFFFFFFFF.toInt())
        }

        fun isMouseOver(mouseX: Int, mouseY: Int): Boolean {
            return mouseX in x..(x + width) && mouseY in y..(y + height)
        }
    }
}