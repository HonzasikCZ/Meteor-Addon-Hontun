package cz.honzasik.hontun.gui.theme.widgets;

import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.theme.HontunWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WHorizontalSeparator;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WHontunHorizontalSeparator extends WHorizontalSeparator implements HontunWidget {
    private final RichText richText;
    private Color color;

    public WHontunHorizontalSeparator(String text) {
        super(text);
        richText = RichText.bold(text);
    }

    @Override
    public void init() {
        super.init();
        color = theme().surface0Color();
    }

    @Override
    protected void onCalculateSize() {
        super.onCalculateSize();
        textWidth = theme().textWidth(richText);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (text == null) renderWithoutText(renderer);
        else renderWithText();
    }

    private void renderWithoutText(GuiRenderer renderer) {
        double s = theme().scale(2);
        double w = width / 2;

        renderer.quad(x, y + s, w, s, color);
        renderer.quad(x + w, y + s, w, s, color);
    }

    private void renderWithText() {
        HontunGuiTheme theme = theme();
        double offsetX = width / 2 - textWidth / 2;
        double offsetY = height / 2 - theme.textHeight() / 2;

        renderer().text(
                richText,
                x + offsetX,
                y + offsetY,
                theme.accentColor()
        );
    }
}
