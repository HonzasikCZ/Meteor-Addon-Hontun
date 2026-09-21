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

/**
 * Hontun restyles the multiplayer list and draws its own move-up/move-down buttons on each
 * server row. Hiding vanilla's arrow sprites does nothing about those, so they get the same
 * treatment here: the row keeps its join button, the arrows go, and the freed column is
 * where the grip is drawn.
 *
 * <p>{@link Pseudo} with a string target is what makes this optional - without Hontun
 * installed the class is simply never patched, and this mod never needs Hontun (or Meteor)
 * on its classpath to build.
 */
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
