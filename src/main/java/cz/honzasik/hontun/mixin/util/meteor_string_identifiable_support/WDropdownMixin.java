package cz.honzasik.hontun.mixin.util.meteor_string_identifiable_support;

import meteordevelopment.meteorclient.gui.widgets.input.WDropdown;
import net.minecraft.util.StringRepresentable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = WDropdown.class, remap = false)
public abstract class WDropdownMixin {
    @Redirect(method = "onCalculateSize", at = @At(value = "INVOKE", target = "Ljava/lang/Object;toString()Ljava/lang/String;"))
    private String redirect(Object instance) {
        return instance instanceof StringRepresentable si ? si.getSerializedName() : instance.toString();
    }
}
