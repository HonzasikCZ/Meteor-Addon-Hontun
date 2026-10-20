package cz.honzasik.hontun.mixin.meteor.titlescreen;

import cz.honzasik.hontun.Hontun;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.utils.player.TitleScreenCredits;
import net.minecraft.util.Util;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.util.List;

@Mixin(value = TitleScreenCredits.class, remap = false)
public abstract class AddonCreditLinkMixin {

    @Unique private static final String HONTUN_URL = "https://github.com/HonzasikCZ/Meteor-Addon-Hontun";

    @Inject(method = "onClicked", at = @At("HEAD"), cancellable = true)
    private static void hontun$openRepo(double mouseX, double mouseY, CallbackInfoReturnable<Boolean> cir) {
        try {
            if (MeteorClient.mc.gui.screen() == null) return;

            Field creditsField = TitleScreenCredits.class.getDeclaredField("credits");
            creditsField.setAccessible(true);
            List<?> credits = (List<?>) creditsField.get(null);
            if (credits == null || credits.isEmpty()) return;

            int lineHeight = MeteorClient.mc.font.lineHeight + 2;
            int y = 3;

            for (Object credit : credits) {
                Class<?> type = credit.getClass();

                Field addonField = type.getDeclaredField("addon");
                addonField.setAccessible(true);
                MeteorAddon addon = (MeteorAddon) addonField.get(credit);

                Field textField = type.getDeclaredField("text");
                textField.setAccessible(true);
                Component text = (Component) textField.get(credit);

                int width;
                synchronized (text) {
                    width = MeteorClient.mc.font.width(text);
                }
                int x = MeteorClient.mc.gui.screen().width - 3 - width;

                boolean hit = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + lineHeight;
                if (hit && addon == Hontun.ADDON) {
                    Util.getPlatform().openUri(HONTUN_URL);
                    cir.setReturnValue(true);
                    return;
                }
                y += lineHeight;
            }
        } catch (Throwable ignored) {
        }
    }
}
