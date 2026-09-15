package cz.honzasik.hontun.gui.widget;

import cz.honzasik.hontun.utils.HontunTheme;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;

import java.util.function.Consumer;
import java.util.function.IntPredicate;

public class HontunTextBox extends EditBox {
    public enum Kind { PLAIN, SEARCH }

    private static final Identifier VANILLA = Identifier.withDefaultNamespace("widget/text_field");
    private static final Identifier VANILLA_FOCUSED = Identifier.withDefaultNamespace("widget/text_field_highlighted");

    private final Font font;
    private final Kind kind;
    private final int padLeft, padRight, padTop;
    private final int boxW, boxH;

    private Runnable onClear;
    private IntPredicate filter;
    private boolean masked;
    private String hintText = "";
    private int hintVersion = -1;
    private int lastMouseX = Integer.MIN_VALUE, lastMouseY = Integer.MIN_VALUE;

    public HontunTextBox(Font font, int x, int y, int w, int h, Kind kind, Component narration) {
        super(font,
                x + leftPad(kind),
                y + (h - 8) / 2,
                Math.max(8, w - leftPad(kind) - (kind == Kind.SEARCH ? 14 : 4)),
                9,
                narration);
        this.font = font;
        this.kind = kind;
        this.padLeft = leftPad(kind);
        this.padRight = kind == Kind.SEARCH ? 14 : 4;
        this.padTop = (h - 8) / 2;
        this.boxW = w;
        this.boxH = h;
        setBordered(false);
        setMaxLength(128);
    }

    private static int leftPad(Kind kind) {
        if (kind == Kind.SEARCH) return 18;
        return HontunTheme.modern() ? 16 : 4;
    }

    public static HontunTextBox plain(Font f, int x, int y, int w, int h, String narration) {
        return new HontunTextBox(f, x, y, w, h, Kind.PLAIN, Component.literal(narration));
    }

    public static HontunTextBox search(Font f, int x, int y, int w, int h, String narration) {
        return new HontunTextBox(f, x, y, w, h, Kind.SEARCH, Component.literal(narration));
    }

    public HontunTextBox maxLength(int n) { setMaxLength(n); return this; }

    public HontunTextBox text(String s) { setValue(s == null ? "" : s); return this; }

    public HontunTextBox placeholder(String s) { hintText = s == null ? "" : s; hintVersion = -1; return this; }

    public HontunTextBox onChange(Consumer<String> c) { setResponder(c); return this; }

    public HontunTextBox onClear(Runnable r) { onClear = r; return this; }

    public HontunTextBox filter(IntPredicate p) { filter = p; return this; }

    public HontunTextBox numeric() { return filter(cp -> cp >= '0' && cp <= '9'); }

    public HontunTextBox masked() {
        masked = true;
        addFormatter((s, off) -> FormattedCharSequence.forward("*".repeat(s.length()), Style.EMPTY));
        return this;
    }

    @Override
    public boolean charTyped(CharacterEvent e) {
        if (filter != null && !filter.test(e.codepoint())) return true;
        return super.charTyped(e);
    }

    @Override
    public void insertText(String s) {
        if (filter != null && s != null) {
            StringBuilder sb = new StringBuilder(s.length());
            s.codePoints().filter(filter).forEach(sb::appendCodePoint);
            s = sb.toString();
        }
        super.insertText(s);
    }

    @Override
    public void onClick(MouseButtonEvent e, boolean doubleClick) {
        if (masked) { moveCursorToEnd(false); return; }
        super.onClick(e, doubleClick);
    }

    @Override
    protected void onDrag(MouseButtonEvent e, double dx, double dy) {
        if (masked) return;
        super.onDrag(e, dx, dy);
    }

    public int boxX() { return getX() - padLeft; }
    public int boxY() { return getY() - padTop; }
    public int boxW() { return boxW; }
    public int boxH() { return boxH; }
    public int boxBottom() { return boxY() + boxH; }

    @Override
    public boolean isMouseOver(double mx, double my) {
        return isActive()
                && mx >= boxX() && mx < boxX() + boxW
                && my >= boxY() && my < boxY() + boxH;
    }

    @Override
    public boolean isHovered() { return isMouseOver(lastMouseX, lastMouseY); }

    @Override
    public ScreenRectangle getRectangle() { return new ScreenRectangle(boxX(), boxY(), boxW, boxH); }

    private boolean clearable() { return kind == Kind.SEARCH; }

    private boolean overClear(double mx, double my) {
        if (!clearable() || getValue().isEmpty()) return false;
        int cx = boxX() + boxW - padRight;
        return mx >= cx && mx < cx + padRight && my >= boxY() && my < boxY() + boxH;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent e, boolean doubleClick) {
        if (isActive() && overClear(e.x(), e.y())) {
            setValue("");
            setFocused(true);
            if (onClear != null) onClear.run();
            return true;
        }
        return super.mouseClicked(e, doubleClick);
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float pt) {
        if (!isVisible()) return;
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        refreshPalette();

        if (HontunTheme.modern2()) {
            drawModern2(g, mouseX, mouseY);
        } else if (HontunTheme.modern()) {
            drawModern(g, mouseX, mouseY);
        } else {
            g.blitSprite(RenderPipelines.GUI_TEXTURED, isFocused() ? VANILLA_FOCUSED : VANILLA,
                    boxX(), boxY(), boxW, boxH);
            if (kind == Kind.SEARCH) {
                drawMagnifier(g, boxX() + 6, boxY() + boxH / 2 - 3, HontunTheme.argb(0xFF,
                        isFocused() ? HontunTheme.subtext1() : HontunTheme.textDim()));
            }
        }

        super.extractWidgetRenderState(g, mouseX, mouseY, pt);
        drawClear(g, mouseX, mouseY);
    }

    private void refreshPalette() {
        boolean modern = HontunTheme.modern();
        setTextColor(modern ? HontunTheme.argb(0xFF, HontunTheme.textLight()) : EditBox.DEFAULT_TEXT_COLOR);
        setTextColorUneditable(modern ? HontunTheme.argb(0xFF, HontunTheme.textDim()) : 0xFF707070);

        int v = HontunTheme.version();
        if (v != hintVersion) {
            hintVersion = v;
            if (!hintText.isEmpty()) {
                setHint(Component.literal(hintText).setStyle(modern
                        ? Style.EMPTY.withColor(TextColor.fromRgb(HontunTheme.overlay2()))
                        : (kind == Kind.SEARCH ? EditBox.SEARCH_HINT_STYLE : EditBox.DEFAULT_HINT_STYLE)));
            }
        }
    }

    private void drawModern(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        boolean focused = isFocused();
        boolean hovered = isMouseOver(mouseX, mouseY);
        int bx = boxX(), by = boxY();

        int fill = focused ? HontunTheme.surface2() : (hovered ? HontunTheme.surface1() : HontunTheme.surface0());
        HontunCards.card(g, bx, by, boxW, boxH, HontunTheme.argb(0xF0, fill));
        HontunCards.border(g, bx, by, boxW, boxH,
                focused ? HontunTheme.argb(0xFF, HontunTheme.accent())
                        : HontunTheme.argb(0x60, HontunTheme.overlay0()));

        g.fillGradient(bx + 2, by + boxH - 2, bx + boxW - 2, by + boxH - 1,
                HontunTheme.argb(focused ? 0xFF : 0x50, HontunTheme.accentLo()),
                HontunTheme.argb(focused ? 0xFF : 0x50, HontunTheme.accentHi()));

        HontunIcons.draw(g, kind == Kind.SEARCH ? HontunIcons.SEARCH : HontunIcons.PERSON,
                bx + 5, by + boxH / 2 - 4,
                HontunTheme.argb(0xFF, focused ? HontunTheme.accentHi() : HontunTheme.textDim()));
    }

    private void drawModern2(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        boolean focused = isFocused();
        boolean hovered = isMouseOver(mouseX, mouseY);
        int bx = boxX(), by = boxY();
        int cut = 4;

        if (focused) HontunShapes.glow(g, bx, by, boxW, boxH, cut, HontunTheme.accent(), 2, 0x50);
        HontunShapes.fillClipped(g, bx, by, boxW, boxH, cut, cut,
                HontunTheme.argb(0xF0, focused ? HontunTheme.surface1() : HontunTheme.crust()));
        HontunShapes.outlineClipped(g, bx, by, boxW, boxH, cut, cut,
                focused ? HontunTheme.argb(0xFF, HontunTheme.accent())
                        : HontunTheme.argb(hovered ? 0xA0 : 0x70, HontunTheme.overlay0()));

        int glyph = kind == Kind.SEARCH ? HontunIcons.SEARCH : HontunIcons.PERSON;
        HontunIcons.draw(g, glyph, bx + 6, by + boxH / 2 - 4,
                HontunTheme.argb(0xFF, focused ? HontunTheme.accentHi() : HontunTheme.textDim()));
    }

    private void drawMagnifier(GuiGraphicsExtractor g, int x, int y, int argb) {
        g.fill(x + 1, y, x + 5, y + 1, argb);
        g.fill(x + 1, y + 5, x + 5, y + 6, argb);
        g.fill(x, y + 1, x + 1, y + 5, argb);
        g.fill(x + 5, y + 1, x + 6, y + 5, argb);
        g.fill(x + 5, y + 5, x + 6, y + 6, argb);
        g.fill(x + 6, y + 6, x + 8, y + 8, argb);
    }

    private void drawClear(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        if (!clearable() || getValue().isEmpty()) return;
        int cx = boxX() + boxW - padRight;
        boolean over = overClear(mouseX, mouseY);
        Component x = Component.literal("✕");
        g.text(font, x, cx + (padRight - font.width(x)) / 2, boxY() + (boxH - 8) / 2,
                HontunTheme.argb(0xFF, over ? HontunTheme.red() : HontunTheme.textDim()), false);
    }
}
