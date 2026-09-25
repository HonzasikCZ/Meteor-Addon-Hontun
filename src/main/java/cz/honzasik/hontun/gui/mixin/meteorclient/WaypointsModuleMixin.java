package cz.honzasik.hontun.gui.mixin.meteorclient;

import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.systems.modules.render.WaypointsModule;
import meteordevelopment.meteorclient.utils.render.color.Color;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = WaypointsModule.class, remap = false)
public abstract class WaypointsModuleMixin {
    @Shadow @Final private static Color GRAY;

    @Redirect(
            method = "initTable",
            at = @At(value = "FIELD", target = "Lmeteordevelopment/meteorclient/systems/modules/render/WaypointsModule;GRAY:Lmeteordevelopment/meteorclient/utils/render/color/Color;", opcode = Opcodes.GETSTATIC)
    )
    private Color hontun$gray() {
        if (GuiThemes.get() instanceof HontunGuiTheme theme && theme.light()) return theme.textSecondaryColor();
        return GRAY;
    }
}
