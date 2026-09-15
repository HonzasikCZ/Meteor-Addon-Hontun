package cz.honzasik.hontun.mixin.util.meteor_string_identifiable_support;

import meteordevelopment.meteorclient.gui.themes.meteor.widgets.input.WMeteorDropdown;
import net.minecraft.util.StringRepresentable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = WMeteorDropdown.class, remap = false)
public abstract class WMeteorDropdownMixin {
    @Redirect(method = "onRender", at = @At(value = "INVOKE", target = "Ljava/lang/Object;toString()Ljava/lang/String;"))
    private String redirect(Object instance) {
        return instance instanceof StringRepresentable si ? si.getSerializedName() : instance.toString();
    }
}
