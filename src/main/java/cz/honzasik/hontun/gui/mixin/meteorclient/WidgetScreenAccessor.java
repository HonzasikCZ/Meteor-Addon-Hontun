package cz.honzasik.hontun.gui.mixin.meteorclient;

import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = WidgetScreen.class, remap = false)
public interface WidgetScreenAccessor {
    @Accessor("root")
    WContainer hontun$root();
}
