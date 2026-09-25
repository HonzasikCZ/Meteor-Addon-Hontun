package cz.honzasik.hontun.gui.widget;

import cz.honzasik.hontun.utils.HontunTheme;
import cz.honzasik.hontun.utils.VfpBridge;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.RenderPipelines;
import cz.honzasik.hontun.utils.VfpIcons;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class VersionGridList extends ObjectSelectionList<VersionGridList.Row> {
    private static final int ITEM_H = 34;
    private static final int GAP = 6;
    private static final int MIN_CELL = 104;

    private final Consumer<Object> onSelect;
    private int cols = 1;
    private Object current;
    private Object autoDetect;

    public VersionGridList(Minecraft mc, int width, int height, int y, Consumer<Object> onSelect) {
        super(mc, width, height, y, ITEM_H);
        this.onSelect = onSelect;
    }

    @Override protected double scrollRate() { return ITEM_H * 2.5; }

    @Override public int getRowWidth() { return getWidth() - 28; }

    @Override protected void extractListBackground(GuiGraphicsExtractor g) {}
    @Override protected void extractListSeparators(GuiGraphicsExtractor g) {}

    @Override
    protected void extractScrollbar(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        if (!HontunTheme.modern() || !scrollable()) { super.extractScrollbar(g, mouseX, mouseY); return; }
        HontunShapes.scrollbar(g, scrollBarX(), getY(), getBottom(), scrollbarWidth(),
                scrollBarY(), scrollerHeight());
    }

    @Override protected void extractSelection(GuiGraphicsExtractor g, Row e, int i) {}

    public double getScroll() { return scrollAmount(); }
    public void setScroll(double s) { setScrollAmount(s); }

    public void set(List<VfpBridge.Version> versions, Object current) {
        this.current = current;
        this.autoDetect = VfpBridge.autoDetect();
        cols = Math.max(1, (getRowWidth() + GAP) / (MIN_CELL + GAP));
        clearEntries();
        for (int i = 0; i < versions.size(); i += cols) {
            addEntry(new Row(new ArrayList<>(versions.subList(i, Math.min(i + cols, versions.size())))));
        }
    }

    private static Component colored(String s, int rgb) {
        return Component.literal(s).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)));
    }

    public class Row extends ObjectSelectionList.Entry<Row> {
        private final List<VfpBridge.Version> cells;

        Row(List<VfpBridge.Version> cells) { this.cells = cells; }

        private int cellW() { return (getWidth() - (cols - 1) * GAP) / cols; }

        @Override
        public void extractContent(GuiGraphicsExtractor g, int mouseX, int mouseY, boolean hovered, float pt) {
            Font f = Minecraft.getInstance().font;
            boolean modern = HontunTheme.modern();
            int cw = cellW();
            int cy = getY() + 2;
            int ch = getHeight() - 4;

            for (int c = 0; c < cells.size(); c++) {
                VfpBridge.Version v = cells.get(c);
                int cx = getX() + c * (cw + GAP);
                boolean cur = v.handle() != null && Objects.equals(v.handle(), current);
                boolean over = mouseX >= cx && mouseX < cx + cw && mouseY >= cy && mouseY < cy + ch;

                if (modern) HontunCards.row(g, cx, cy, cw, ch, cur, over, HontunTheme.accent());
                else if (HontunTheme.restyleEnabled()) HontunCards.vanillaStyledCell(g, cx, cy, cw, ch, cur || over);
                else HontunCards.vanillaButtonCell(g, cx, cy, cw, ch, cur || over);

                int nameCol = cur ? (modern ? HontunTheme.accentHi() : HontunTheme.accent())
                        : HontunTheme.textLight();
                int left = cx + 5 + HontunCards.rowTextInset(cur);
                int right = cx + cw - 5;

                Identifier icon = VfpIcons.forVersion(v.name(), v.handle() != null && v.handle() == autoDetect);
                if (icon != null && cw >= 56) {
                    g.blit(RenderPipelines.GUI_TEXTURED, icon, left, cy + (ch - 16) / 2,
                            0.0f, 0.0f, 16, 16, 16, 16, 16, 16);
                    left += 20;
                }

                boolean smoothDot = cur && HontunRound.smoothDots();
                Component nameText = cz.honzasik.hontun.utils.HontunFont.apply(
                        colored((cur && !smoothDot ? "● " : "") + v.name(), nameCol));
                if (smoothDot) {
                    int tw = f.width(nameText);
                    int group = tw + 7;
                    boolean fits = group <= right - left;
                    int gx = fits ? left + (right - left - group) / 2 : left;
                    HontunRound.dot(g, gx + 1.75f, cy + 3 + 4f, 1.75f, HontunTheme.argb(0xFF, nameCol));
                    g.textRenderer().acceptScrollingWithDefaultCenter(nameText,
                            gx + 7, fits ? gx + 7 + tw : right, cy + 3, cy + 3 + f.lineHeight);
                } else {
                    g.textRenderer().acceptScrollingWithDefaultCenter(nameText,
                            left, right, cy + 3, cy + 3 + f.lineHeight);
                }

                g.textRenderer().acceptScrollingWithDefaultCenter(
                        cz.honzasik.hontun.utils.HontunFont.apply(colored("protocol " + v.id(), HontunTheme.textDim())),
                        left, right, cy + ch - 3 - f.lineHeight, cy + ch - 3);
            }
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent e, boolean doubleClick) {
            int cw = cellW();
            int local = (int) (e.x() - getX());
            int idx = local / (cw + GAP);
            if (local - idx * (cw + GAP) >= cw) return true;
            if (idx >= 0 && idx < cells.size()) onSelect.accept(cells.get(idx).handle());
            return true;
        }

        @Override
        public Component getNarration() {
            return Component.literal(cells.isEmpty() ? "" : cells.get(0).name());
        }
    }
}
