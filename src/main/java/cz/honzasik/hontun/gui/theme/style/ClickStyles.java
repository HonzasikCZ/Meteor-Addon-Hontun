package cz.honzasik.hontun.gui.theme.style;

import cz.honzasik.hontun.gui.theme.style.chamfer.ChamferClickStyle;
import cz.honzasik.hontun.gui.theme.style.flat.FlatClickStyle;
import cz.honzasik.hontun.gui.theme.style.hvanilla.HVanillaClickStyle;
import cz.honzasik.hontun.gui.theme.style.smog.SmogClickStyle;
import cz.honzasik.hontun.gui.theme.style.vanilla.VanillaClickStyle;
import cz.honzasik.hontun.utils.HontunTheme;

import java.util.EnumMap;
import java.util.Map;

public final class ClickStyles {
    private static final Map<HontunTheme.UiMode, ClickStyle> STYLES = new EnumMap<>(HontunTheme.UiMode.class);

    static {
        register(new VanillaClickStyle());
        register(new HVanillaClickStyle());
        register(new FlatClickStyle());
        register(new ChamferClickStyle());
        register(new SmogClickStyle());
    }

    private ClickStyles() {}

    private static void register(ClickStyle style) {
        STYLES.put(style.id(), style);
    }

    public static ClickStyle of(HontunTheme.UiMode mode) {
        ClickStyle style = mode == null ? null : STYLES.get(mode);
        return style != null ? style : STYLES.get(HontunTheme.UiMode.HModern2);
    }
}
