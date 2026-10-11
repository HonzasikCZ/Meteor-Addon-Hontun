package cz.honzasik.hontun.mixin.client.cursor;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.cursor.CursorType;
import cz.honzasik.hontun.gui.cursor.HontunCursors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public abstract class WindowCursorMixin {
    @Shadow private CursorType currentCursor;

    @Inject(method = "selectCursor", at = @At("HEAD"))
    private void hontun$refreshThemedCursor(CursorType type, CallbackInfo ci) {
        if (HontunCursors.stale()) currentCursor = null;
    }
}
