package cz.honzasik.hontun.mixin.client.button;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import cz.honzasik.hontun.gui.widget.HontunButtons;
import cz.honzasik.hontun.utils.HontunTheme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ImageButton.class)
public abstract class RecipeButtonStyleMixin {
    @Shadow @Final protected WidgetSprites sprites;

    @WrapOperation(method = "extractContents", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"))
    private void hontun$recipeButton(GuiGraphicsExtractor g, RenderPipeline pipeline, Identifier sprite,
                                     int x, int y, int w, int h, Operation<Void> op) {
        if (!HontunTheme.restyleEnabled() || sprites != RecipeBookComponent.RECIPE_BUTTON_SPRITES) {
            op.call(g, pipeline, sprite, x, y, w, h);
            return;
        }
        AbstractWidget self = (AbstractWidget) (Object) this;
        boolean active = self.active;
        boolean hovered = active && self.isHoveredOrFocused();
        HontunButtons.background(g, x, y, w, h, active, hovered);
        if (HontunTheme.smog()) {
            cz.honzasik.hontun.gui.render.RoundedGui.outline(g, (float) x, (float) y, (float) w, (float) h,
                    Math.min(8, Math.min(w, h) / 2), 1f, HontunTheme.argb(hovered ? 0x70 : 0x38, 0xFFFFFF));
        }
        g.blit(RenderPipelines.GUI_TEXTURED, BOOK_ICON, x + (w - 16) / 2, y + (h - 14) / 2,
                0.0f, 0.0f, 16, 14, 16, 14);
    }

    private static final Identifier BOOK_ICON =
            Identifier.fromNamespaceAndPath("hontun", "textures/gui/recipe_book_icon.png");
}
