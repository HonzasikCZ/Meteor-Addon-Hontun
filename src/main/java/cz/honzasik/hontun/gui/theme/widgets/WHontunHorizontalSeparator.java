package cz.honzasik.hontun.gui.theme.widgets;

import cz.honzasik.hontun.gui.api.text.RichText;
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
        style().paintSeparatorH(this, renderer, mouseX, mouseY);
    }

    public boolean hasText() {
        return text != null;
    }

    public String titleString() {
        return text;
    }

    public RichText richText() {
        return richText;
    }

    public double titleWidth() {
        return textWidth;
    }

    public Color lineColor() {
        return color;
    }
}
