package cz.honzasik.hontun.gui.mixin.meteorclient;

import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = WTextBox.class, remap = false)
public abstract class WTextBoxMixin extends WWidget {
}
