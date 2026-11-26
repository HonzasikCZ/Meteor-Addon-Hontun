package com.mrmelon54.DraggableLists.mixin.hontun;

import com.mrmelon54.DraggableLists.DraggableLists;
import com.mrmelon54.DraggableLists.render.HontunJoinZone;
import com.mrmelon54.DraggableLists.theme.Themes;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "cz.honzasik.hontun.utils.HontunServerCard", remap = false)
public class HontunServerCardMixin {
    @Inject(method = "arrows", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void dl$replaceArrowsWithGrip(GuiGraphicsExtractor graphics, int contentX, int contentY,
                                                 int index, int count, int mouseX, int mouseY, CallbackInfo ci) {
        if (!DraggableLists.config().hideServerArrows) return;
        if (!DraggableLists.config().serverDragging.isEnabled()) return;

        HontunJoinZone.draw(graphics, contentX, contentY, mouseX, mouseY, Themes.current());
        ci.cancel();
    }
}
