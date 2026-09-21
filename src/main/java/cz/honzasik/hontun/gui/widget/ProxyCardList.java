package cz.honzasik.hontun.gui.widget;

import cz.honzasik.hontun.utils.HontunFont;
import cz.honzasik.hontun.utils.HontunTheme;
import meteordevelopment.meteorclient.systems.proxies.Proxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

public class ProxyCardList extends ObjectSelectionList<ProxyCardList.ProxyEntry> {
    private static final int ITEM_H = 36;
    private static final int DEL = 20;

    private final Consumer<Proxy> onToggle;
    private final Consumer<Proxy> onDelete;

    public ProxyCardList(Minecraft mc, int width, int height, int y,
                         Consumer<Proxy> onToggle, Consumer<Proxy> onDelete) {
        super(mc, width, height, y, ITEM_H);
        this.onToggle = onToggle;
        this.onDelete = onDelete;
    }

    @Override protected double scrollRate() { return ITEM_H * 2.0; }

    @Override public int getRowWidth() { return getWidth() - 28; }

    @Override protected void extractListBackground(GuiGraphicsExtractor g) {}
    @Override protected void extractListSeparators(GuiGraphicsExtractor g) {}

    @Override
    protected void extractScrollbar(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        if (!HontunTheme.modern() || !scrollable()) { super.extractScrollbar(g, mouseX, mouseY); return; }
        HontunShapes.scrollbar(g, scrollBarX(), getY(), getBottom(), scrollbarWidth(),
                scrollBarY(), scrollerHeight());
    }

    @Override
    protected void extractSelection(GuiGraphicsExtractor g, ProxyEntry e, int outlineColor) {
        if (HontunTheme.modern()) return;
        HontunCards.vanillaSelection(g, e.getX(), e.getY(), e.getWidth(), e.getHeight(), isFocused());
    }

    public double getScroll() { return scrollAmount(); }
    public void setScroll(double s) { setScrollAmount(s); }

    public void set(List<Proxy> proxies) {
        clearEntries();
        for (Proxy p : proxies) addEntry(new ProxyEntry(p));
    }

    private static int statusColor(Proxy p) {
        String s = p.status != null ? p.status.name() : "UNCHECKED";
        return switch (s) {
            case "ALIVE" -> HontunTheme.green();
            case "DEAD" -> HontunTheme.red();
            case "CHECKING" -> HontunTheme.yellow();
            default -> HontunTheme.textDim();
        };
    }

    public class ProxyEntry extends ObjectSelectionList.Entry<ProxyEntry> {
        private final Proxy proxy;

        ProxyEntry(Proxy proxy) { this.proxy = proxy; }

        @Override
        public void extractContent(GuiGraphicsExtractor g, int mouseX, int mouseY, boolean hovered, float pt) {
            Font f = Minecraft.getInstance().font;
            boolean modern = HontunTheme.modern();
            int x = getX(), w = getWidth();
            int inset = modern ? 3 : 0;
            int cy = getY() + inset;
            int ch = getHeight() - inset * 2;
            boolean on = Boolean.TRUE.equals(proxy.enabled.get());

            HontunCards.row(g, x, cy, w, ch, on, hovered, HontunTheme.green());

            int dotX = modern ? x + 10 + HontunCards.rowTextInset(on) : x + 4;
            g.text(f, Component.literal(on ? "●" : "○"), dotX, cy + (ch - f.lineHeight) / 2,
                    HontunTheme.argb(0xFF, on ? HontunTheme.green() : HontunTheme.textDim()), true);

            int tx = modern ? dotX + 12 : x + 16;
            int nameY = modern ? cy + 6 : cy + 3;
            int subY = modern ? cy + 6 + f.lineHeight + 3 : cy + 14;

            String st = proxy.status != null ? proxy.status.name() : "UNCHECKED";
            String lat = proxy.latency > 0 ? " " + proxy.latency + "ms" : "";
            Component stc = HontunFont.apply(Component.literal(st + lat));
            int statusW = f.width(stc);
            int statusX = x + w - DEL - (modern ? 12 : 8) - statusW;

            int minTextRoom = 40;
            boolean showStatus = statusX >= tx + minTextRoom;
            int textRight = showStatus ? statusX - 6 : delX() - 6;
            int room = Math.max(0, textRight - tx);

            g.text(f, HontunFont.apply(HontunCards.clip(f, String.valueOf(proxy.name.get()), room)), tx, nameY,
                    HontunTheme.argb(0xFF, on ? HontunTheme.textLight() : HontunTheme.subtext1()), true);
            String sub = proxy.address.get() + ":" + proxy.port.get() + "   [" + proxy.type.get() + "]";
            g.text(f, HontunFont.apply(HontunCards.clip(f, sub, room)), tx, subY,
                    HontunTheme.argb(0xFF, HontunTheme.textDim()), true);

            if (showStatus) {
                g.text(f, stc, statusX, cy + (ch - f.lineHeight) / 2,
                        HontunTheme.argb(0xFF, statusColor(proxy)), true);
            }

            if (hovered) HontunCards.deleteButton(g, f, delX(), delY(), DEL, mouseX, mouseY);
        }

        private int delX() { return getX() + getWidth() - DEL - (HontunTheme.modern() ? 6 : 2); }

        private int delY() {
            int i = HontunTheme.modern() ? 3 : 0;
            return getY() + i + ((getHeight() - 2 * i) - DEL) / 2;
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent e, boolean doubleClick) {
            int bx = delX(), by = delY();
            if (e.x() >= bx && e.x() < bx + DEL && e.y() >= by && e.y() < by + DEL) {
                onDelete.accept(proxy);
            } else {
                setSelected(this);
                onToggle.accept(proxy);
            }
            return true;
        }

        @Override
        public Component getNarration() {
            return Component.literal(String.valueOf(proxy.name.get()));
        }
    }
}
