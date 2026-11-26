package cz.honzasik.hontun.gui.mixin.meteorclient;

import meteordevelopment.meteorclient.gui.GuiThemes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.io.File;

@Mixin(value = GuiThemes.class, remap = false)
public interface GuiThemesAccessor {
    @Accessor("THEMES_FOLDER")
    static File hontun$themesFolder() {
        throw new AssertionError();
    }
}
