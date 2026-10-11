package cz.honzasik.hontun.gui.theme.widgets;

import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.render.text.GlyphSupport;
import cz.honzasik.hontun.gui.theme.HontunWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WMultiLabel;
import meteordevelopment.meteorclient.utils.render.color.Color;

import java.util.List;

public class WHontunMultiLabel extends WMultiLabel implements HontunWidget {
    public WHontunMultiLabel(RichText text, double maxWidth) {
        super(text.getPlainText(), false, maxWidth);
    }

    @Override
    protected void onCalculateSize() {
        if (maxWidth == 0 || !GlyphSupport.hasCjk(text)) super.onCalculateSize();
        else wrapCjk();

        double total = 0;
        for (int i = 0; i < lines.size(); i++) total += lineHeight(i);
        height = total;
    }

    private void wrapCjk() {
        lines.clear();
        double limit = theme.scale(maxWidth);
        double space = theme.textWidth(" ", 1, title);
        double widest = 0;
        StringBuilder sb = new StringBuilder();

        for (String line : text.split("\n")) {
            double lineWidth = 0;
            boolean content = false;

            for (String word : line.split(" ")) {
                List<String> units = GlyphSupport.breakUnits(word);
                for (int u = 0; u < units.size(); u++) {
                    String unit = units.get(u);
                    double w = theme.textWidth(unit, unit.length(), title);
                    boolean spaced = u == 0 && content;
                    double add = w + (spaced ? space : 0);

                    if (content && lineWidth + add > limit) {
                        lines.add(sb.toString());
                        sb.setLength(0);
                        sb.append(unit);
                        lineWidth = w;
                    } else {
                        if (spaced) sb.append(' ');
                        sb.append(unit);
                        lineWidth += add;
                    }

                    content = true;
                    widest = Math.max(widest, lineWidth);
                }
            }

            lines.add(sb.toString());
            sb.setLength(0);
        }

        width = widest;
    }

    private double lineHeight(int i) {
        double h = theme.textHeight(title);
        if (theme().richText() && GlyphSupport.needsVanilla(lines.get(i))) h = Math.max(h, theme().vanillaHeight(1));
        return h;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        Color defaultColor = style().labelColor(theme(), title);
        double lineY = y;

        for (int i = 0; i < lines.size(); i++) {
            renderer().text(
                    RichText.of(lines.get(i)).boldIf(title),
                    x,
                    lineY,
                    color != null ? color : defaultColor
            );
            lineY += lineHeight(i);
        }
    }
}
