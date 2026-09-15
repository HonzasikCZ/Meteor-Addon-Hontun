package cz.honzasik.hontun.mixin.client.titlescreen;

import cz.honzasik.hontun.utils.HontunTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LogoRenderer.class)
public abstract class HontunLogoMixin {
    private static final Identifier HONTUN_LOGO =
            Identifier.fromNamespaceAndPath("hontun", "textures/gui/icon_logo.png");
    private static final int TEX = 256;

    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IFI)V",
            at = @At("HEAD"), cancellable = true)
    private void hontun$drawLogo(GuiGraphicsExtractor g, int width, float alpha, int heightOffset, CallbackInfo ci) {
        if (!HontunTheme.restyleEnabled()) return;

        LogoRenderer self = (LogoRenderer) (Object) this;
        float a = self.keepLogoThroughFade() ? 1.0f : Mth.clamp(alpha, 0.0f, 1.0f);
        int tint = HontunTheme.argb(Math.round(a * 255f), HontunTheme.accentHi());

        int size, x, y;
        if (heightOffset == LogoRenderer.DEFAULT_HEIGHT_OFFSET) {
            int guiH = Minecraft.getInstance().getWindow().getGuiScaledHeight();
            int firstButtonY = guiH / 4 + 48;
            size = Mth.clamp(Math.round(firstButtonY * 0.50f), 48, 88);
            y = Math.max(4, firstButtonY / 2 - size / 2);
        } else {
            size = 72;
            y = heightOffset + 25 - size / 2;
        }
        x = width / 2 - size / 2;

        int shadowA = Math.round(a * 90f);
        if (shadowA > 0) {
            g.blit(RenderPipelines.GUI_TEXTURED, HONTUN_LOGO, x + 3, y + 4, 0.0f, 0.0f,
                    size, size, TEX, TEX, TEX, TEX, HontunTheme.argb(shadowA, 0x000000));
            g.blit(RenderPipelines.GUI_TEXTURED, HONTUN_LOGO, x + 1, y + 2, 0.0f, 0.0f,
                    size, size, TEX, TEX, TEX, TEX, HontunTheme.argb(shadowA / 2, 0x000000));
        }

        int glowA = Math.round(a * 150f);
        if (glowA > 0) {
            for (int i = 1; i <= 5; i++) {
                int gs = size + i * 8;
                int off = (gs - size) / 2;
                g.blit(RenderPipelines.GUI_TEXTURED, HONTUN_LOGO, x - off, y - off, 0.0f, 0.0f,
                        gs, gs, TEX, TEX, TEX, TEX,
                        HontunTheme.argb(Math.max(1, glowA / (i * i + 2)), HontunTheme.accentHi()));
            }
        }

        g.blit(RenderPipelines.GUI_TEXTURED, HONTUN_LOGO, x, y, 0.0f, 0.0f,
                size, size, TEX, TEX, TEX, TEX, tint);

        ci.cancel();
    }
}
