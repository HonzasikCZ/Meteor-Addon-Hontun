package cz.honzasik.hontun.gui.render;

import cz.honzasik.hontun.gui.api.render.RoundedRect;
import cz.honzasik.hontun.gui.api.render.RoundedRectRenderer;
import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.render.route.PrimitiveRouter;
import cz.honzasik.hontun.gui.render.route.Routers;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.render.rounded.RoundedRendererInternal;
import cz.honzasik.hontun.gui.render.text.HontunTextRenderer;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.renderer.Texture;
import meteordevelopment.meteorclient.utils.render.color.Color;

import cz.honzasik.hontun.gui.render.rounded.modern.RoundedRendererModern;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class HontunRenderer implements RoundedRectRenderer {
    private static final HontunRenderer INSTANCE = new HontunRenderer();
    private static final List<Runnable> STYLE_LISTENERS = new CopyOnWriteArrayList<>();
    private static boolean flatText;
    private static boolean clickGuiTextPass;

    public static boolean flatText() {
        return flatText;
    }

    public static void setFlatText(boolean flat) {
        flatText = flat;
    }

    public static boolean clickGuiTextPass() {
        return clickGuiTextPass;
    }

    public static void setClickGuiTextPass(boolean pass) {
        clickGuiTextPass = pass;
    }
    public static GuiRenderer guiRenderer;

    private HontunGuiTheme theme;

    private final RoundedRendererInternal roundedRenderer = new RoundedRendererModern();

    private final HontunTextRenderer textRenderer = new HontunTextRenderer();

    private boolean clipEnabled = false;
    private float clipMinX;
    private float clipMinY;
    private float clipMaxX;
    private float clipMaxY;

    private double globalAlpha = 1;
    private GuiGraphicsExtractor graphics;
    private Texture atlas;

    public static HontunRenderer get() {
        return INSTANCE;
    }

    public static void addStyleListener(Runnable listener) {
        STYLE_LISTENERS.add(listener);
    }

    public void setTheme(HontunGuiTheme theme) {
        if (this.theme == null) this.theme = theme;
    }

    public HontunGuiTheme theme() {
        return theme;
    }

    public PrimitiveRouter router() {
        return theme == null ? Routers.LEGACY : theme.router();
    }

    public double globalAlpha() {
        return globalAlpha;
    }

    public double onSetAlpha(double a) {
        globalAlpha = a;
        return router().alpha(a);
    }

    public void styleChanged() {
        globalAlpha = 1;
        for (Runnable listener : STYLE_LISTENERS) {
            try {
                listener.run();
            } catch (Throwable ignored) {
            }
        }
    }

    public void setFrame(GuiGraphicsExtractor graphics, Texture atlas) {
        this.graphics = graphics;
        this.atlas = atlas;
    }

    public GuiGraphicsExtractor graphics() {
        return graphics;
    }

    public Texture atlas() {
        return atlas;
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
        if (router().richText(text, x, y, color)) return;

        if (guiRenderer != null && theme != null && !theme.richText())
            guiRenderer.text(text.getPlainText(), x, y, color, false);

        else textRenderer.text(text, x, y, color, theme);
    }

    @Override
    public void renderRoundedRect(RoundedRect rect) {
        if (router().roundedRect(rect)) return;

        roundedRenderer.render(rect);
    }

    public void flipFrame() {
        roundedRenderer.flipFrame();
    }
}
