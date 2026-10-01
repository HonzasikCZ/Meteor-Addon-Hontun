package cz.honzasik.hontun.gui.render.route;

import cz.honzasik.hontun.gui.api.render.RoundedRect;
import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.gui.widgets.containers.WView;
import meteordevelopment.meteorclient.renderer.Texture;
import meteordevelopment.meteorclient.utils.render.color.Color;

public interface PrimitiveRouter {
    default boolean quad4(GuiRenderer r, double x, double y, double w, double h, Color tl, Color tr, Color br, Color bl) {
        return false;
    }

    default boolean texQuad(GuiRenderer r, double x, double y, double w, double h, GuiTexture t, Color c) {
        return false;
    }

    default boolean rotatedTexQuad(GuiRenderer r, double x, double y, double w, double h, double rot, GuiTexture t, Color c) {
        return false;
    }

    default boolean triangle(GuiRenderer r, double x1, double y1, double x2, double y2, double x3, double y3, Color c) {
        return false;
    }

    default boolean texture(GuiRenderer r, double x, double y, double w, double h, double rot, Texture t) {
        return false;
    }

    default boolean meteorText(GuiRenderer r, String s, double x, double y, Color c, boolean title) {
        return false;
    }

    default boolean richText(RichText t, double x, double y, Color c) {
        return false;
    }

    default boolean roundedRect(RoundedRect rect) {
        return false;
    }

    default double alpha(double a) {
        return a;
    }

    default void windowLayer(GuiRenderer r) {
    }

    default boolean skipTextPass() {
        return false;
    }

    default double measure(HontunGuiTheme t, RichText text) {
        return -1;
    }

    default void afterViewUpdate(WView v) {
    }
}
