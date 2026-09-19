package cz.honzasik.hontun.mixin.client.multiplayer;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSelectionList.class)
public abstract class SelectionOutlineMixin {
    @Inject(method = "extractSelection", at = @At("HEAD"), cancellable = true)
    private void hontun$noOutline(GuiGraphicsExtractor g, AbstractSelectionList.Entry<?> entry,
                                  int outlineColor, CallbackInfo ci) {
        // Vanilla only draws this box while the row is focused, so it vanishes the moment the
        // user clicks Edit/Join/Delete and you can no longer tell what is selected. Hontun draws
        // its own selection marker per theme in ServerCardMixin, keyed on the list's selection
        // rather than focus, so vanilla's is suppressed for the server list in every mode.
        if ((Object) this instanceof ServerSelectionList) ci.cancel();
    }
}
