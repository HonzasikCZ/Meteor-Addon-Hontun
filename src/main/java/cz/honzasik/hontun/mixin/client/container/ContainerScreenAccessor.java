package cz.honzasik.hontun.mixin.client.container;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface ContainerScreenAccessor {
    @Accessor("imageWidth")
    int hontun$imageWidth();

    @Accessor("imageHeight")
    int hontun$imageHeight();
}
