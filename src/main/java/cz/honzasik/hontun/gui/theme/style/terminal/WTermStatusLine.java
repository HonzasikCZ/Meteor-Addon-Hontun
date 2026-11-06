package cz.honzasik.hontun.gui.theme.style.terminal;

import com.mojang.blaze3d.platform.InputConstants;
import cz.honzasik.hontun.gui.screen.ActiveLabel;
import cz.honzasik.hontun.gui.theme.HontunWidget;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.Locale;

import static cz.honzasik.hontun.gui.theme.style.terminal.TerminalPalette.opaque;

public class WTermStatusLine extends WWidget implements HontunWidget {
    private final WTermConfig config;
    private double activeX0, activeX1, copyX0, copyX1, pasteX0, pasteX1;

    public WTermStatusLine(WTermConfig config) {
        this.config = config;
    }

    public WTermConfig config() {
        return config;
    }

    private String activeLabel(Module module) {
        String label = module instanceof ActiveLabel a ? a.activeLabel() : "active";
        return label.toLowerCase(Locale.ROOT).replace(' ', '-');
    }

    @Override
    protected void onCalculateSize() {
        TerminalGrid g = TerminalGrid.of(theme());
        width = 0;
        height = g.st;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        TerminalGrid g = TerminalGrid.of(theme());
        double ty = g.ty(y + 1, height - 1);
        Module module = config.module();

        TerminalPaint.rect(x, y, width, height, TerminalPalette.barA());
        TerminalPaint.hline(x, y, width, opaque(TerminalPalette.line));

        String mode = " " + config.mode().name() + " ";
        double cx = x;
        TerminalPaint.rect(cx, y + 1, mode.length() * g.cw, height - 1, opaque(TerminalPalette.fill));
        TerminalPaint.text(mode, cx, ty, opaque(TerminalPalette.ink));
        cx += mode.length() * g.cw;

        String file = " " + config.fileName();
        TerminalPaint.text(file, cx, ty, opaque(TerminalPalette.fg));
        cx += file.length() * g.cw;
        if (TerminalState.dirty(config.dirtyKey())) TerminalPaint.text(" [+]", cx, ty, opaque(TerminalPalette.acc));

        int total = config.lineCount();
        int current = Math.max(1, config.cursorNumber());
        String digits = String.valueOf(total);
        String pos = "  " + " ".repeat(Math.max(0, digits.length() - String.valueOf(current).length())) + current + "/" + total + " ";

        double rx = x + width - pos.length() * g.cw;
        TerminalPaint.text(pos, rx, ty, opaque(TerminalPalette.dim));

        String paste = " :r paste ";
        rx -= paste.length() * g.cw;
        pasteX0 = rx;
        pasteX1 = rx + paste.length() * g.cw;
        command(paste, rx, mouseX, mouseY, pasteX0, pasteX1, ty);

        rx -= g.cw;
        String copy = " :w copy ";
        rx -= copy.length() * g.cw;
        copyX0 = rx;
        copyX1 = rx + copy.length() * g.cw;
        command(copy, rx, mouseX, mouseY, copyX0, copyX1, ty);

        if (module == null) {
            activeX0 = activeX1 = 0;
            return;
        }

        rx -= g.cw;
        String label = " " + activeLabel(module) + " ";
        int cols = 4 + label.length();
        rx -= cols * g.cw;
        activeX0 = rx;
        activeX1 = rx + cols * g.cw;
        if (over(mouseX, mouseY, activeX0, activeX1)) TerminalPaint.rect(activeX0, y + 1, activeX1 - activeX0, height - 1, TerminalPalette.hl());
        TerminalPaint.text("[", rx + g.cw, ty, opaque(TerminalPalette.dim));
        TerminalPaint.text(module.isActive() ? "x" : " ", rx + 2 * g.cw, ty, opaque(TerminalPalette.acc));
        TerminalPaint.text("]", rx + 3 * g.cw, ty, opaque(TerminalPalette.dim));
        TerminalPaint.text(label, rx + 4 * g.cw, ty, opaque(TerminalPalette.fg));
    }

    private boolean over(double mx, double my, double x0, double x1) {
        return x1 > x0 && my >= y && my <= y + height && mx >= x0 && mx <= x1;
    }

    private void command(String text, double rx, double mx, double my, double x0, double x1, double ty) {
        if (over(mx, my, x0, x1)) TerminalPaint.rect(x0, y + 1, x1 - x0, height - 1, TerminalPalette.hl());
        TerminalPaint.text(text, rx, ty, opaque(TerminalPalette.fg));
    }

    @Override
    public boolean onMouseClicked(MouseButtonEvent click, boolean doubled) {
        if (click.button() != InputConstants.MOUSE_BUTTON_LEFT) return false;
        double mx = click.x(), my = click.y();

        Module module = config.module();
        if (module != null && over(mx, my, activeX0, activeX1)) {
            module.toggle();
            return true;
        }
        if (over(mx, my, copyX0, copyX1)) {
            copy();
            return true;
        }
        if (over(mx, my, pasteX0, pasteX1)) {
            paste();
            return true;
        }
        return false;
    }

    private void copy() {
        WidgetScreen screen = config.screen();
        if (screen == null) return;
        if (screen instanceof TerminalModuleScreen) {
            screen.toClipboard();
            return;
        }

        String file = config.fileName();
        if (screen.toClipboard()) {
            TerminalState.dirty(config.dirtyKey(), false);
            TerminalLog.post("\"" + file + "\" " + config.lineCount() + "L copied to clipboard");
        }
        else {
            TerminalLog.post("\"" + file + "\": nothing to copy here");
        }
    }

    private void paste() {
        WidgetScreen screen = config.screen();
        if (screen == null) return;
        if (screen instanceof TerminalModuleScreen) {
            screen.fromClipboard();
            return;
        }

        String file = config.fileName();
        if (screen.fromClipboard()) {
            TerminalState.dirty(config.dirtyKey(), false);
            config.reload();
            TerminalLog.post("\"" + file + "\" pasted from clipboard");
        }
        else {
            TerminalLog.post("\"" + file + "\": clipboard holds no config for it");
        }
    }
}
