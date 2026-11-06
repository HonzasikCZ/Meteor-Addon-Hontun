package cz.honzasik.hontun.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.FontDescription;

public final class HontunFont {
    private static final Identifier SMOG = Identifier.fromNamespaceAndPath("hontun", "smog");
    private static final FontDescription SMOG_FONT = new FontDescription.Resource(SMOG);
    private static final Identifier MONO_ID = Identifier.fromNamespaceAndPath("hontun", "mono");
    public static final FontDescription MONO = new FontDescription.Resource(MONO_ID);

    private HontunFont() {}

    public static boolean active() {
        return HontunTheme.smog() || HontunTheme.terminal();
    }

    private static FontDescription current() {
        return HontunTheme.terminal() ? MONO : SMOG_FONT;
    }

    public static MutableComponent text(String s) {
        MutableComponent c = Component.literal(s == null ? "" : s);
        if (!active()) return c;
        FontDescription f = current();
        return c.withStyle(st -> st.withFont(f));
    }

    public static Component apply(Component c) {
        if (c == null || !active()) return c;
        FontDescription f = current();
        return c.copy().withStyle((Style st) -> st.withFont(f));
    }
}
