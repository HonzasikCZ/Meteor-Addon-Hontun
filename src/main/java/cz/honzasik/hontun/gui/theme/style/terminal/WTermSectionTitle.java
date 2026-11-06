package cz.honzasik.hontun.gui.theme.style.terminal;

import cz.honzasik.hontun.gui.theme.HontunWidget;
import cz.honzasik.hontun.gui.theme.widgets.container.WHontunSection;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WWidget;

import java.util.Locale;

public class WTermSectionTitle extends WWidget implements HontunWidget {
    private final WHontunSection section;
    private final String label;

    public WTermSectionTitle(WHontunSection section, String title) {
        this.section = section;
        this.label = title == null ? "" : title.toLowerCase(Locale.ROOT).replace(' ', '-');
    }

    public WHontunSection section() {
        return section;
    }

    public String label() {
        return label;
    }

    @Override
    protected void onCalculateSize() {
        TerminalGrid g = TerminalGrid.of(theme());
        width = (label.length() + 2 + 6) * g.cw;
        height = g.lh;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (style() instanceof TerminalClickStyle style) style.paintSectionTitle(this);
    }
}
