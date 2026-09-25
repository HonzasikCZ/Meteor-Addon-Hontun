package cz.honzasik.hontun.mixin.client.container;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import cz.honzasik.hontun.gui.widget.HontunContainers;
import cz.honzasik.hontun.utils.HontunTheme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.DispenserScreen;
import net.minecraft.client.gui.screens.inventory.HopperScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({InventoryScreen.class, CraftingScreen.class, ContainerScreen.class, ShulkerBoxScreen.class,
        HopperScreen.class, DispenserScreen.class, AbstractFurnaceScreen.class})
public abstract class ContainerPanelMixin {
    @WrapOperation(method = "extractBackground", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIII)V"))
    private void hontun$panel(GuiGraphicsExtractor g, RenderPipeline pipeline, Identifier texture, int x, int y,
                              float u, float v, int w, int h, int texW, int texH, Operation<Void> op) {
        if (!HontunTheme.restyleEnabled()) {
            op.call(g, pipeline, texture, x, y, u, v, w, h, texW, texH);
            return;
        }
        if (v != 0f) return;
        HontunContainers.draw(g, (AbstractContainerScreen<?>) (Object) this, x, y);
    }
}
