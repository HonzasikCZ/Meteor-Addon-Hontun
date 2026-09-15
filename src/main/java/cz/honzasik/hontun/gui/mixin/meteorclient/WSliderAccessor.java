package cz.honzasik.hontun.gui.mixin.meteorclient;

import meteordevelopment.meteorclient.gui.widgets.input.WSlider;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = WSlider.class, remap = false)
public interface WSliderAccessor {
}
