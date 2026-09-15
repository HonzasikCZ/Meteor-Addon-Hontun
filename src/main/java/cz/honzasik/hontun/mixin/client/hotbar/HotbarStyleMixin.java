package cz.honzasik.hontun.mixin.client.hotbar;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import cz.honzasik.hontun.utils.HontunTheme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Hud.class)
public abstract class HotbarStyleMixin {
    private static final String BLIT = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V";

    @Redirect(method = "extractItemHotbar", at = @At(value = "INVOKE", target = BLIT, ordinal = 0))
    private void hontun$hotbarFrame(GuiGraphicsExtractor g, RenderPipeline pipeline, Identifier sprite, int x, int y, int w, int h) {
        if (!HontunTheme.restyleEnabled()) { g.blitSprite(pipeline, sprite, x, y, w, h); return; }

        int accent = HontunTheme.accent();
        if (HontunTheme.modern()) {
            g.fill(x, y, x + w, y + h, HontunTheme.argb(0x66, HontunTheme.surface0()));
            g.fill(x, y + h - 1, x + w, y + h, HontunTheme.argb(0x99, accent));
        } else {
            int border = HontunTheme.argb(0xCC, accent);
            g.fill(x, y, x + w, y + h, HontunTheme.argb(0xB0, HontunTheme.base()));
            g.fill(x, y, x + w, y + 1, border);
            g.fill(x, y + h - 1, x + w, y + h, border);
            g.fill(x, y, x + 1, y + h, border);
            g.fill(x + w - 1, y, x + w, y + h, border);
        }
    }

    @Redirect(method = "extractItemHotbar", at = @At(value = "INVOKE", target = BLIT, ordinal = 1))
    private void hontun$hotbarSelection(GuiGraphicsExtractor g, RenderPipeline pipeline, Identifier sprite, int x, int y, int w, int h) {
        if (!HontunTheme.restyleEnabled()) { g.blitSprite(pipeline, sprite, x, y, w, h); return; }

        int c = HontunTheme.argb(0xFF, HontunTheme.accentHi());
        g.fill(x, y, x + w, y + 1, c);
        g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c);
        g.fill(x + w - 1, y, x + w, y + h, c);
    }
}
