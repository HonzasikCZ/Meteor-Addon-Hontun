package cz.honzasik.hontun.gui.mixin.meteorclient;

import meteordevelopment.meteorclient.gui.widgets.containers.WView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = WView.class, remap = false)
public interface WViewScrollAccessor {
    @Accessor("scroll")
    double hontun$getScroll();

    @Accessor("scroll")
    void hontun$setScroll(double scroll);

    @Accessor("targetScroll")
    double hontun$getTargetScroll();

    @Accessor("targetScroll")
    void hontun$setTargetScroll(double targetScroll);

    @Accessor("actualHeight")
    double hontun$getActualHeight();
}
