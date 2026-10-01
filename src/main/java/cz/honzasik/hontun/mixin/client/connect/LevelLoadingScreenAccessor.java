package cz.honzasik.hontun.mixin.client.connect;

import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.multiplayer.LevelLoadTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LevelLoadingScreen.class)
public interface LevelLoadingScreenAccessor {
    @Accessor("loadTracker")
    LevelLoadTracker hontun$loadTracker();

    @Accessor("reason")
    LevelLoadingScreen.Reason hontun$reason();
}
