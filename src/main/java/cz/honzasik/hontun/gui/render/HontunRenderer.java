package cz.honzasik.hontun.gui.render;

import cz.honzasik.hontun.gui.api.render.RoundedRect;
import cz.honzasik.hontun.gui.api.render.RoundedRectRenderer;
import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.render.rounded.RoundedRendererInternal;
import cz.honzasik.hontun.gui.render.text.HontunTextRenderer;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.utils.render.color.Color;

import cz.honzasik.hontun.gui.render.rounded.modern.RoundedRendererModern;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public class HontunRenderer implements RoundedRectRenderer {
    private static final HontunRenderer INSTANCE = new HontunRenderer();
    public static GuiRenderer guiRenderer;

    private HontunGuiTheme theme;

    private final RoundedRendererInternal roundedRenderer = new RoundedRendererModern();

    private final HontunTextRenderer textRenderer = new HontunTextRenderer();

    private boolean clipEnabled = false;
    private float clipMinX;
    private float clipMinY;
    private float clipMaxX;
    private float clipMaxY;

    public static HontunRenderer get() {
        return INSTANCE;
    }

    public void setTheme(HontunGuiTheme theme) {
        if (this.theme == null) this.theme = theme;
    }

    public void begin() {
        roundedRenderer.begin();
    }

    public void end() {
        roundedRenderer.end();
    }

    public void renderText(

            GuiGraphicsExtractor graphics
    ) {
        if (theme == null) return;

        textRenderer.render(

                graphics,
                theme
        );
    }

    public void setClipRect(double minX, double minY, double maxX, double maxY) {
        clipEnabled = true;
        clipMinX = (float) minX;
        clipMinY = (float) minY;
        clipMaxX = (float) maxX;
        clipMaxY = (float) maxY;
    }

    public void clearClipRect() {
        clipEnabled = false;
        clipMinX = 0f;
        clipMinY = 0f;
        clipMaxX = 0f;
        clipMaxY = 0f;
    }

    public boolean isClipEnabled() {
        return clipEnabled;
    }

    public float getClipMinX() { return clipMinX; }
    public float getClipMinY() { return clipMinY; }
    public float getClipMaxX() { return clipMaxX; }
    public float getClipMaxY() { return clipMaxY; }

    public void text(RichText text, double x, double y, Color color) {
        if (guiRenderer != null && !Config.get().customFont.get())
            guiRenderer.text(text.getPlainText(), x, y, color, false);

        else textRenderer.text(text, x, y, color, theme);
    }

    @Override
    public void renderRoundedRect(double x, double y,
                                  double width, double height,
                                  float rTopLeft, float rTopRight,
                                  float rBottomLeft, float rBottomRight,
                                  Color fillColor, Color outlineColor, float outlineWidth
    ) {
        roundedRenderer.render(
                x, y,
                width, height,
                rTopLeft, rTopRight,
                rBottomLeft, rBottomRight,
                fillColor, outlineColor, outlineWidth
        );
    }

    public void flipFrame() {
        roundedRenderer.flipFrame();
    }
}
