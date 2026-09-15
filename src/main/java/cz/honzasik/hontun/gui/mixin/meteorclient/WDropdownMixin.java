package cz.honzasik.hontun.gui.mixin.meteorclient;

import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.input.WDropdown;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = WDropdown.class, remap = false)
public abstract class WDropdownMixin extends WWidget {
}
