package cz.honzasik.hontun.gui.theme.style.terminal;

import com.mojang.blaze3d.platform.InputConstants;
import cz.honzasik.hontun.gui.theme.HontunWidget;
import cz.honzasik.hontun.gui.theme.widgets.container.WHontunWindow;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class WTermTitle extends WWidget implements HontunWidget {
    private final WHontunWindow window;
    private Category category;
    private boolean resolved;
    private final int[] counts = new int[2];

    public WTermTitle(WHontunWindow window) {
        this.window = window;
    }

    public WHontunWindow window() {
        return window;
    }

    public Category category() {
        if (!resolved) {
            resolved = true;
            if (!window.isDialog()) {
                String title = window.titleText();
                for (Category c : Modules.loopCategories()) {
                    if (c.name.equals(title)) {
                        category = c;
                        break;
                    }
                }
            }
        }
        return category;
    }

    public int[] counts() {
        Category c = category();
        int on = 0, all = 0;

        if (c != null) {
            List<Module> hidden = Config.get().hiddenModules.get();
            for (Module module : Modules.get().getGroup(c)) {
                if (hidden.contains(module)) continue;
                all++;
                if (module.isActive()) on++;
            }
        }

        counts[0] = on;
        counts[1] = all;
        return counts;
    }

    @Override
    protected void onCalculateSize() {
        TerminalGrid g = TerminalGrid.of(theme());
        width = 4 * g.cw;
        height = g.tb;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (style() instanceof TerminalClickStyle style) style.paintTitle(this);
    }

    private boolean overFirstDot(double mx, double my) {
        TerminalGrid g = TerminalGrid.of(theme());
        double d = g.px(8);
        double hh = window.headerWidget().height - 1;
        double x = window.x + g.px(9);
        double y = window.y + Math.round((hh - d) / 2);
        double slop = g.px(3);
        return mx >= x - slop && mx <= x + d + slop && my >= y - slop && my <= y + d + slop;
    }

    @Override
    public boolean onMouseClicked(MouseButtonEvent click, boolean doubled) {
        if (click.button() != InputConstants.MOUSE_BUTTON_LEFT || !overFirstDot(click.x(), click.y())) return false;

        if (window.isDialog()) {
            Screen screen = mc.gui.screen();
            if (screen != null) screen.onClose();
        }
        else {
            window.setExpanded(!window.isExpanded());
        }

        return true;
    }
}
