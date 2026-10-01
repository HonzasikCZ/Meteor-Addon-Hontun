package cz.honzasik.hontun.gui.theme.style.smog;

import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.theme.widgets.WHontunLabel;
import cz.honzasik.hontun.gui.theme.widgets.container.WHontunWindow;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;

public class SmogTitle extends WHontunLabel {
    private final WHontunWindow window;

    public SmogTitle(WHontunWindow window, RichText text) {
        super(text);
        this.window = window;
        this.titleRole = true;
    }

    @Override
    protected void onCalculateSize() {
        super.onCalculateSize();

        if (theme().style() instanceof SmogClickStyle style) style.layoutWindow(window, this);
    }

    @Override
    public void drawLabel(GuiRenderer renderer, double mouseX, double mouseY) {
        if (hidden || text.isEmpty()) return;

        renderer().text(
                richText,
                Math.round(x),
                Math.round(y),
                color != null ? color : theme().textColor()
        );
    }
}
