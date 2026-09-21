package cz.honzasik.hontun.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.FontDescription;

public final class HontunFont {
    private static final Identifier SMOG = Identifier.fromNamespaceAndPath("hontun", "smog");
    private static final FontDescription SMOG_FONT = new FontDescription.Resource(SMOG);

    private HontunFont() {}

    public static MutableComponent text(String s) {
        MutableComponent c = Component.literal(s == null ? "" : s);
        return HontunTheme.smog() ? c.withStyle(st -> st.withFont(SMOG_FONT)) : c;
    }

    public static Component apply(Component c) {
        if (c == null || !HontunTheme.smog()) return c;
        return c.copy().withStyle((Style st) -> st.withFont(SMOG_FONT));
    }
}
