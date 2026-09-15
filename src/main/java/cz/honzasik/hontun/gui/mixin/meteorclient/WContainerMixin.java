package cz.honzasik.hontun.gui.mixin.meteorclient;

import cz.honzasik.hontun.gui.widget.IWidgetBackport;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = WContainer.class, remap = false)
public abstract class WContainerMixin extends WWidget implements IWidgetBackport {
}
