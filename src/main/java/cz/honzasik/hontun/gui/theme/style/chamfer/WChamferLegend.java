package cz.honzasik.hontun.gui.theme.style.chamfer;

import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.theme.HontunWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WChamferLegend extends WWidget implements HontunWidget {
    private final RichText text;

    public WChamferLegend(String title) {
        text = ChamferPaint.upper(title == null ? "" : title, true);
    }

    public RichText text() {
        return text;
    }

    @Override
    protected void onCalculateSize() {
        HontunGuiTheme t = theme();
        int u = ChamferPaint.u(t);
        width = t.textWidth(text) + 2 * 5 * u;
        height = t.textHeight(text);
    }

    public int textLeft() {
        return ChamferPaint.px(x + width / 2 - theme().textWidth(text) / 2);
    }

    public int textRight() {
        return textLeft() + ChamferPaint.px(theme().textWidth(text));
    }

    public int gapLeft() {
        return textLeft() - 7 * ChamferPaint.u(theme());
    }

    public int gapRight() {
        return textRight() + 7 * ChamferPaint.u(theme());
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        HontunGuiTheme t = theme();
        if (text.getPlainText().isEmpty()) return;

        int u = ChamferPaint.u(t);
        GuiFill g = GuiFill.of(renderer);
        Color c = ChamferPaint.edgeText(t);
        int argb = ChamferPaint.argb(0xFF, c);

        int left = textLeft();
        int right = textRight();
        int mid = ChamferPaint.px(y + height / 2) - u / 2;

        ChamferPaint.rect(g, left - 5 * u, mid, left - 2 * u, mid + u, argb);
        ChamferPaint.rect(g, right + 2 * u, mid, right + 5 * u, mid + u, argb);

        ChamferPaint.drawText(text, left, y, c);
    }
}
