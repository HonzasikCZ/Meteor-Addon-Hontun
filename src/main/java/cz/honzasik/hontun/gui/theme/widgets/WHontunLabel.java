package cz.honzasik.hontun.gui.theme.widgets;

import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.theme.HontunWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WLabel;

public class WHontunLabel extends WLabel implements HontunWidget {
    protected RichText richText;
    public boolean hidden;
    public boolean titleRole;

    public WHontunLabel(RichText text) {
        super(text.getPlainText(), false);
        richText = text;
    }

    @Override
    protected void onCalculateSize() {
        width = theme().textWidth(richText);
        height = theme().textHeight(richText);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        drawLabel(renderer, mouseX, mouseY);
    }

    public void drawLabel(GuiRenderer renderer, double mouseX, double mouseY) {
        if (hidden || text.isEmpty()) return;

        renderer().text(
                richText,
                x,
                y,
                color != null ? color : style().labelColor(theme(), titleRole)
        );
    }

    public RichText richText() {
        return richText;
    }

    public void set(RichText text) {
        if (Math.round(theme().textWidth(text)) != width) invalidate();

        this.text = text.getPlainText();
        richText = text;
    }

    @Override
    public void set(String text) {
        set(RichText.of(text));
    }
}
