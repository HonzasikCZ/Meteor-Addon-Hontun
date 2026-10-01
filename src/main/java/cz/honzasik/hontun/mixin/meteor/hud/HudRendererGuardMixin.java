package cz.honzasik.hontun.mixin.meteor.hud;

import cz.honzasik.hontun.gui.render.HontunRenderer;
import meteordevelopment.meteorclient.renderer.text.VanillaTextRenderer;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = HudRenderer.class, remap = false)
public abstract class HudRendererGuardMixin {
    @Inject(method = "begin(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V", at = @At("HEAD"))
    private void hontun$resetTextState(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        VanillaTextRenderer.INSTANCE.setAlpha(1);
        HontunRenderer.setFlatText(false);
        HontunRenderer.setClickGuiTextPass(false);
    }
}
