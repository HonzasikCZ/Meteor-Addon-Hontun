package cz.honzasik.hontun.mixin.client.container;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import cz.honzasik.hontun.gui.widget.HontunContainers;
import cz.honzasik.hontun.utils.HontunFont;
import cz.honzasik.hontun.utils.HontunTheme;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.client.gui.screens.inventory.DispenserScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({AbstractContainerScreen.class, InventoryScreen.class})
public abstract class ContainerLabelMixin {
    @WrapOperation(method = "extractLabels", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V"))
    private void hontun$label(GuiGraphicsExtractor g, Font font, Component text, int x, int y, int color,
                              boolean shadow, Operation<Void> op) {
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        if (!HontunTheme.restyleEnabled() || !HontunContainers.themed(screen)) {
            op.call(g, font, text, x, y, color, shadow);
            return;
        }
        boolean title = text == screen.getTitle();
        Component styled = HontunFont.apply(text);
        if (title && (screen instanceof DispenserScreen || screen instanceof AbstractFurnaceScreen<?>)) {
            x = (((ContainerScreenAccessor) screen).hontun$imageWidth() - font.width(styled)) / 2;
        }
        int rgb = title ? HontunTheme.textLight() : HontunTheme.subtext1();
        op.call(g, font, styled, x, y, 0xFF000000 | rgb, false);
    }
}
