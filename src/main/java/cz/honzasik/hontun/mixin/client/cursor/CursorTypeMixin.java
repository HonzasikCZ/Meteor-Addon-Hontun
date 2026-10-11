package cz.honzasik.hontun.mixin.client.cursor;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.cursor.CursorType;
import cz.honzasik.hontun.gui.cursor.HontunCursors;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CursorType.class)
public abstract class CursorTypeMixin {
    @Inject(method = "select", at = @At("HEAD"), cancellable = true)
    private void hontun$themedCursor(Window window, CallbackInfo ci) {
        if (!HontunCursors.enabled()) {
            HontunCursors.release();
            HontunCursors.destroyRetired();
            return;
        }
        long handle = HontunCursors.handle(this.toString());
        if (handle == 0L) return;
        GLFW.glfwSetCursor(window.handle(), handle);
        HontunCursors.destroyRetired();
        ci.cancel();
    }
}
