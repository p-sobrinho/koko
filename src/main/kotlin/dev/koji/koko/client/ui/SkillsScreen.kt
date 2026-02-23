package dev.koji.koko.client.ui

import com.mojang.blaze3d.systems.RenderSystem
import dev.koji.koko.common.SkillsHandler
import dev.koji.koko.common.helpers.MainHelper
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
import net.minecraft.world.item.component.ItemLore

class SkillsScreen : Screen(Component.literal("Skills Screen")) {
    companion object {
        private const val MAX_SLOTS_PER_LINE = 6
        private const val SLOT_SIZE = 20
        private const val INITIAL_OFFSET_X = 15
        private const val OFFSET_X = 4
        private const val OFFSET_Y = 4

        private val BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/demo_background.png")
        private val ITEM_STACK_CACHE = mutableMapOf<ResourceLocation, ItemStack>()

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
            val line = i / MAX_SLOTS_PER_LINE
            val collum = i % MAX_SLOTS_PER_LINE

            val x = xPosition + INITIAL_OFFSET_X + (collum * (SLOT_SIZE + OFFSET_X))
            val y = slotsStartY + (line * (SLOT_SIZE + OFFSET_Y))

            val skillModel = SkillsHandler.getSkillModel(player, key)
                ?: continue

            skillSlots.add(SkillSlot(x, y, SLOT_SIZE, SLOT_SIZE, key, value, skillModel))

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

                val skillIcon = getItemStackFromRL(MainHelper.safeParseResource(skillModel.icon)).apply {
                    remove(DataComponents.ATTRIBUTE_MODIFIERS)

                    set(DataComponents.CUSTOM_NAME, Component.translatable(skillModel.displayName))

                    val currentLevel = SkillsHandler.getLevel(player, slot.skillLocation)
                    val maxLevel =
                        if (slot.skillData.isUnlocked) skillModel.unlockedMaxLevel else skillModel.maxLevel

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

    override fun isPauseScreen(): Boolean = false

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
                MainHelper.safeParseResource(skillModel.icon)
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