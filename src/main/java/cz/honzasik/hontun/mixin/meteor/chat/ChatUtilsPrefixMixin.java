package cz.honzasik.hontun.mixin.meteor.chat;

import cz.honzasik.hontun.utils.HontunChat;
import cz.honzasik.hontun.utils.HontunTheme;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ChatUtils.class, remap = false)
public abstract class ChatUtilsPrefixMixin {
    @Shadow
    private static Component PREFIX;

    @Unique
    private static boolean hontun$prefixListenerAdded = false;

    @Inject(method = "init", at = @At("TAIL"))
    private static void hontun$rebrandPrefix(CallbackInfo ci) {
        PREFIX = HontunChat.prefix();

        if (!hontun$prefixListenerAdded) {
            hontun$prefixListenerAdded = true;
            HontunTheme.addListener(() -> PREFIX = HontunChat.prefix());
        }
    }

    @Inject(method = "formatMsg", at = @At("HEAD"), cancellable = true)
    private static void hontun$recolorBody(String msg, ChatFormatting color, CallbackInfoReturnable<MutableComponent> cir) {
        if (color == ChatFormatting.GRAY) {
            cir.setReturnValue(HontunChat.formatInfo(msg));
        }
    }

    @Inject(method = "getCustomPrefix", at = @At("HEAD"), cancellable = true)
    private static void hontun$customTag(String title, ChatFormatting color, CallbackInfoReturnable<MutableComponent> cir) {
        cir.setReturnValue(HontunChat.tag(title));
    }
}
