package dev.koji.koko.mixins;

import dev.koji.koko.common.events.PlayerEventHandler;
import dev.koji.koko.common.helpers.MainHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingMenu.class)
public abstract class CraftingMenuMixin {
    @Inject(method = "slotChangedCraftingGrid", at = @At("RETURN"))
    private static void slotChangedCraftingGridAfter(
            AbstractContainerMenu container, Level level, Player player,
            CraftingContainer craftContainer, ResultContainer resultContainer,
            RecipeHolder<CraftingRecipe> recipeHolder, CallbackInfo ci
    ) {
        if (level.isClientSide) return;

        if (!PlayerEventHandler.INSTANCE.isItemBlockedFor(
                player, resultContainer.getItem(0), PlayerEventHandler.PlayerBlockScope.CRAFT
        )) return;

        resultContainer.setItem(0, ItemStack.EMPTY);

        MainHelper.INSTANCE.sendMessageToPlayer(
                player, PlayerEventHandler.DefaultPlayerMessages.INSTANCE.getUNABLE_TO_CRAFT()
        );
    }
}
