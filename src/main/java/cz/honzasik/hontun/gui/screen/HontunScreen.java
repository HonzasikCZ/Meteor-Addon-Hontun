package cz.honzasik.hontun.gui.screen;

import cz.honzasik.hontun.gui.widget.HontunCards;
import cz.honzasik.hontun.gui.widget.HontunShapes;
import cz.honzasik.hontun.utils.HontunTheme;
import cz.honzasik.hontun.utils.MenuBackground;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class HontunScreen extends Screen {
    protected static final int HEADER_H = 33;
    protected static final int FOOTER_H = 33;

    protected static final int STATUS_H = 11;
    private static final int SIDE_MARGIN = 20;
    private static final int BLOCK_MAX = 502;

    private static final int TITLE_H = 22;

    protected final Screen parent;

    protected int px, py, pw, ph;

    protected HontunScreen(Component title, Screen parent) {
        super(title);
        this.parent = parent;
    }

    protected abstract void buildContent();

    @Override
    protected void init() {
        pw = Math.max(200, Math.min(760, width - 40));
        ph = Math.max(150, Math.min(520, height - 30));
        px = (width - pw) / 2;
        py = (height - ph) / 2;

        buildContent();

        int bx = HontunTheme.modern() ? px + pw / 2 - 100 : width / 2 - 100;
        int by = HontunTheme.modern() ? py + ph - 28 : height - 27;
        addRenderableWidget(Button.builder(Component.literal("Back"), b -> onClose())
                .bounds(bx, by, 200, 20)
                .build());
    }

    private int blockW() { return Math.min(width - 2 * SIDE_MARGIN, BLOCK_MAX); }
    private int blockX() { return (width - blockW()) / 2; }

    protected int bandBottom() { return height - FOOTER_H; }

    protected int contentTop()    { return HontunTheme.modern() ? py + TITLE_H : HEADER_H; }

    protected int contentBottom() { return HontunTheme.modern() ? py + ph - 48 : bandBottom() - STATUS_H; }
    protected int contentLeft()   { return HontunTheme.modern() ? px + 14 : blockX(); }
    protected int contentRight()  { return HontunTheme.modern() ? px + pw - 14 : blockX() + blockW(); }
    protected int contentWidth()  { return HontunTheme.modern() ? pw - 28 : blockW(); }

    protected int statusY() { return HontunTheme.modern() ? py + ph - 40 : bandBottom() - STATUS_H + 1; }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        if (!HontunTheme.restyleEnabled()) {
            super.extractBackground(g, mouseX, mouseY, delta);
            return;
        }

        if (HontunTheme.modern2()) {
            MenuBackground.render(g, width, height, true);
            HontunShapes.panel(g, px, py, pw, ph, 12);
            HontunShapes.titleChip(g, font, px + pw / 2, py, getTitle());
            return;
        }

        if (HontunTheme.modern()) {
            MenuBackground.render(g, width, height, true);
            g.fill(px, py, px + pw, py + ph, HontunTheme.argb(0xF2, HontunTheme.surface0()));
            g.fillGradient(px, py, px + pw, py + 2,
                    HontunTheme.argb(0xFF, HontunTheme.accentLo()),
                    HontunTheme.argb(0xFF, HontunTheme.accentHi()));
            int b = HontunTheme.argb(0x70, HontunTheme.overlay0());
            g.fill(px, py + ph - 1, px + pw, py + ph, b);
            g.fill(px, py + 2, px + 1, py + ph, b);
            g.fill(px + pw - 1, py + 2, px + pw, py + ph, b);

            int tw = font.width(getTitle());
            int tx = px + pw / 2 - tw / 2;
            g.text(font, getTitle(), tx, py + 7, HontunTheme.argb(0xFF, HontunTheme.textLight()), true);
            g.fill(tx, py + 18, tx + tw, py + 19, HontunTheme.argb(0xC0, HontunTheme.accentHi()));
            return;
        }

        if (minecraft.level != null) extractBlurredBackground(g);
        else MenuBackground.render(g, width, height, true);

        extractMenuBackground(g);

        HontunCards.vanillaListBackground(g, 0, contentTop(), width, bandBottom() - contentTop(), 0);

        HontunCards.vanillaSeparators(g, 0, contentTop(), bandBottom(), width);

        int tw = font.width(getTitle());
        int tx = width / 2 - tw / 2;
        g.text(font, getTitle(), tx, 12, HontunTheme.argb(0xFF, HontunTheme.textLight()), true);
        g.fill(tx, 23, tx + tw, 24, HontunTheme.argb(0xFF, HontunTheme.accentHi()));
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }
}
