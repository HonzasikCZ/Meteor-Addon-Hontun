package cz.honzasik.hontun.gui.widget;

import cz.honzasik.hontun.utils.HontunTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class HontunDotButton extends Button.Plain {
    private final boolean dot;
    private final boolean smooth;
    private final int color;

    public HontunDotButton(int x, int y, int w, int h, String text, boolean dot, OnPress onPress) {
        this(x, y, w, h, text, dot, HontunRound.smoothDots() && dotted(), onPress);
    }

    private HontunDotButton(int x, int y, int w, int h, String text, boolean dot, boolean smooth, OnPress onPress) {
        super(x, y, w, h, label(text, dot, smooth), onPress, DEFAULT_NARRATION);
        this.dot = dot;
        this.smooth = smooth;
        this.color = selectedColor();
    }

    public boolean selected() {
        return dot;
    }

    private static boolean dotted() {
        return !HontunTheme.modern2() && !HontunTheme.hvanilla();
    }

    private static int selectedColor() {
        return HontunTheme.modern() ? HontunTheme.accentHi() : HontunTheme.accent();
    }

    private static Component label(String text, boolean sel, boolean smooth) {
        if (!sel) return Component.literal(text);
        String s;
        if (HontunTheme.modern2()) s = "– " + text + " –";
        else if (HontunTheme.hvanilla()) s = "» " + text + " «";
        else s = smooth ? text : "● " + text;
        return Component.literal(s).withColor(selectedColor());
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float pt) {
        super.extractContents(g, mouseX, mouseY, pt);
        if (!dot || !smooth) return;
        int tw = Minecraft.getInstance().font.width(getMessage());
        float cx = getX() + getWidth() / 2f - tw / 2f - 5.5f;
        float cy = getY() + getHeight() / 2f;
        HontunRound.dot(g, cx, cy, 1.75f, HontunTheme.argb(active ? 0xFF : 0x80, color));
    }
}
