package cz.honzasik.hontun.gui.theme.style.chamfer;

import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.api.text.TextScale;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.theme.HontunWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;

import java.util.List;
import java.util.Locale;

public class WHontunReadout extends WWidget implements HontunWidget {
    private final Category category;

    private int active = -1;
    private int total = -1;
    private RichText activeText = RichText.of("");
    private RichText totalText = RichText.of("");

    public WHontunReadout(Category category) {
        this.category = category;
    }

    public Category category() {
        return category;
    }

    @Override
    protected void onCalculateSize() {
        refresh();

        HontunGuiTheme t = theme();
        width = t.textWidth(activeText) + t.textWidth(totalText);
        height = t.textHeight(activeText);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (refresh()) invalidate();

        HontunGuiTheme t = theme();
        double x0 = Math.round(x);
        double y0 = Math.round(y);

        ChamferPaint.drawText(activeText, x0, y0, t.accentColor());
        ChamferPaint.drawText(totalText, x0 + t.textWidth(activeText), y0, t.light() ? ChamferPaint.subtext1(t) : t.textSecondaryColor());
    }

    private boolean refresh() {
        int on = 0;
        int all = 0;

        List<Module> hidden = Config.get().hiddenModules.get();
        for (Module module : Modules.get().getGroup(category)) {
            if (hidden.contains(module)) continue;
            all++;
            if (module.isActive()) on++;
        }

        if (on == active && all == total) return false;

        boolean resized = all != total;
        active = on;
        total = all;

        int digits = String.valueOf(all).length();
        double small = TextScale.SMALL.get();
        activeText = RichText.of(String.format(Locale.ROOT, "%0" + digits + "d", on)).scale(small);
        totalText = RichText.of("/" + all).scale(small);

        return resized;
    }
}
