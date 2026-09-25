package cz.honzasik.hontun.gui.theme.widgets;

import cz.honzasik.hontun.gui.api.animation.Animation;
import cz.honzasik.hontun.gui.api.animation.Direction;
import cz.honzasik.hontun.gui.api.animation.Easing;
import cz.honzasik.hontun.gui.api.render.Corners;
import cz.honzasik.hontun.gui.api.text.RichText;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.theme.HontunWidget;
import cz.honzasik.hontun.gui.util.ColorUtils;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.Color;

import java.util.ArrayList;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

import meteordevelopment.meteorclient.systems.config.Config;

public class WHontunModule extends WPressable implements HontunWidget {
    private final Module module;
    private final RichText title;

    private double titleWidth;
    private boolean wasHovered = false;
    private boolean wasActive = false;

    private Module prevModule;
    private Module nextModule;

    private Animation highlightAnimation;
    private Animation hoverAnimation;

    public WHontunModule(Module module, String title) {
        this.module = module;
        this.title = RichText.of(title);
        this.tooltip = module.description;
    }

    @Override
    public void init() {
        boolean isActive = module.isActive();
        wasActive = isActive;

        List<Module> visibleModules = new ArrayList<>(Modules.get().getGroup(module.category));

        visibleModules.removeAll(Config.get().hiddenModules.get());

        int visibleIndex = visibleModules.indexOf(module);

        if (visibleIndex > 0)
            prevModule = visibleModules.get(visibleIndex - 1);

        if (visibleIndex >= 0 && visibleIndex < visibleModules.size() - 1)
            nextModule = visibleModules.get(visibleIndex + 1);

        highlightAnimation = new Animation(
                Easing.QUART_OUT,
                300,
                isActive ? Direction.FORWARDS : Direction.BACKWARDS
        );

        hoverAnimation = new Animation(Easing.LINEAR, 200);
    }

    @Override
    protected void onCalculateSize() {
        double pad = pad();
        if (titleWidth == 0) titleWidth = theme().textWidth(title);

        width = pad + pad + titleWidth + pad;
        height = pad + theme.textHeight() + pad;
    }

    @Override
    protected void onPressed(int button) {
        if (button == GLFW_MOUSE_BUTTON_LEFT)
            module.toggle();

        else if (button == GLFW_MOUSE_BUTTON_RIGHT)
            mc.gui.setScreen(theme.moduleScreen(module));
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        HontunGuiTheme theme = theme();
        boolean moduleActive = module.isActive();
        double pad = pad();

        if (moduleActive != wasActive) {
            wasActive = moduleActive;

            highlightAnimation.start(moduleActive ? Direction.FORWARDS : Direction.BACKWARDS);
        }

        if (mouseOver != wasHovered) {
            wasHovered = mouseOver;

            if (mouseOver) hoverAnimation.start();

            else hoverAnimation.finishedAt(Direction.BACKWARDS);
        }

        double hoverProgress = hoverAnimation.getProgress();
        double highlightProgress = highlightAnimation.getProgress();

        if (hoverProgress > 0 || highlightProgress > 0) {
            int baseAlpha = theme.light() ? 38 : 60;
            double hoverMultiplier = (mouseOver && moduleActive) ? 1.3f : 1.0f;
            double mix = Math.min(1.0, highlightProgress + hoverProgress);
            double alpha = baseAlpha * mix * hoverMultiplier;

            Color color = ColorUtils.withAlpha(theme.accentColor(), (int) alpha);
            Corners bgCorners = (mouseOver && !moduleActive) ? Corners.ALL : corners();

            double maxOffset = 1.5;
            double offset = maxOffset * (1.0 - highlightProgress);

            roundedRect().pos(x + offset, y + offset)
                         .size(width - (offset * 2), height - (offset * 2)).
                         color(color)
                         .radius(smallRadius(), bgCorners)
                         .render();
        }

        double lineWidth = theme.scale(4);

        if (highlightProgress > 0) {
            Corners corners = corners();

            double offset = theme.scale(3);
            double offsetTop = isPrevActive() ? 0 : offset;
            double offsetBottom = isNextActive() ? 0 : offset;

            double lineHeight = height - offsetTop - offsetBottom;

            double finalTop = y + offsetTop;
            double centerY = finalTop + lineHeight / 2;

            double lineX = x + pad;
            double lineY = centerY - (lineHeight * highlightProgress)/ 2;

            roundedRect().pos(lineX, lineY)
                         .size(lineWidth, lineHeight * highlightProgress)
                         .color(ColorUtils.withAlpha(theme.accentColor(), highlightProgress))
                         .radius(smallRadius(), corners)
                         .render();
        }

        double x = this.x;
        double w = width;

        switch (theme.moduleAlignment.get()) {
            case Center -> x += w / 2 - titleWidth / 2;
            case Right -> x += w - titleWidth - pad * 2;
            default -> x += pad + lineWidth + pad;
        }

        Color color = ColorUtils.interpolateColor(
                theme.textColor(),
                theme.accentColor(),
                highlightProgress
        );

        renderer().text(title, x, y + pad, color);
    }

    @Override
    public Corners corners() {
        boolean prev = isPrevActive(), next = isNextActive();
        return prev && next ? Corners.NONE :
                prev ? Corners.BOTTOM :
                next ? Corners.TOP : Corners.ALL;
    }

    private boolean isPrevActive() {
        return prevModule != null && prevModule.isActive();
    }

    private boolean isNextActive() {
        return nextModule != null && nextModule.isActive();
    }
}
