package cz.honzasik.hontun.gui.mixin.meteorclient;

import cz.honzasik.hontun.gui.widget.IWidgetBackport;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WView;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = WView.class, remap = false)
public abstract class WViewMixin extends WContainer implements IWidgetBackport {
}
