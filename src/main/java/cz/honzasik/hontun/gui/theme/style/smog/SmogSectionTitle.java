package cz.honzasik.hontun.gui.theme.style.smog;

import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.api.text.TextScale;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.theme.HontunWidget;
import cz.honzasik.hontun.gui.theme.widgets.container.WHontunSection;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WWidget;

public class SmogSectionTitle extends WWidget implements HontunWidget {
    private final WHontunSection section;
    private final RichText label;

    public SmogSectionTitle(WHontunSection section, String title) {
        this.section = section;
        this.label = RichText.of(title).scale(TextScale.SMALL.get());
    }

    public WHontunSection section() {
        return section;
    }

    public RichText label() {
        return label;
    }

    @Override
    protected void onCalculateSize() {
        HontunGuiTheme t = theme();
        width = t.textWidth(label);
        height = t.textHeight(label);

        if (t.style() instanceof SmogClickStyle style) style.layoutSection(this);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (theme().style() instanceof SmogClickStyle style) style.paintSectionTitle(this, renderer);
    }
}
