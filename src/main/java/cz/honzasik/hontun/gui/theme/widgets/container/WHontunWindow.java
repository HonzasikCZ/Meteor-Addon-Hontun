package cz.honzasik.hontun.gui.theme.widgets.container;

import cz.honzasik.hontun.gui.api.animation.Animation;
import cz.honzasik.hontun.gui.api.animation.Direction;
import cz.honzasik.hontun.gui.api.animation.Easing;
import cz.honzasik.hontun.gui.api.render.Corners;
import cz.honzasik.hontun.gui.screen.HontunModulesScreen;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.theme.HontunWidget;
import cz.honzasik.hontun.gui.util.ColorUtils;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.utils.WindowConfig;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import meteordevelopment.meteorclient.gui.widgets.pressable.WTriangle;
import meteordevelopment.meteorclient.utils.render.color.Color;

import net.minecraft.client.input.MouseButtonEvent;

public class WHontunWindow extends WWindow implements HontunWidget {
    private static final int SHADOW_OFFSET = 2;

    private HontunModulesScreen modulesScreen;
    private boolean shouldSnap = false;
    private int gridSize;

    private double mouseOffsetX;
    private double mouseOffsetY;

    private double contentOffsetY;

    private Animation animation;
    private Animation cornerAnimation;

    public WHontunWindow(WWidget icon, String title) {
        super(icon, title);
    }

    @Override
    public void init() {
        super.init();

        animation = new Animation(
                theme().guiAnimationEasing(),
                theme().guiAnimationDuration(),
                expanded ? Direction.FORWARDS : Direction.BACKWARDS
        );
        cornerAnimation = new Animation(
                Easing.QUART_OUT,
                200,
                expanded ? Direction.FORWARDS : Direction.BACKWARDS
        );
    }

    @Override
    protected void onCalculateSize() {
        super.onCalculateSize();
        minWidth = theme().scale(200);
    }

    public void initSnapping(HontunModulesScreen modulesScreen, int gridSize) {
        this.modulesScreen = modulesScreen;
        this.gridSize = gridSize;
        shouldSnap = true;
    }

    public <T extends WWidget> Cell<T> addDirect(T widget) {
        widget.parent = this;
        widget.theme = theme;

        Cell<T> cell = new Cell<>(widget).centerY();
        cells.add(cell);

        widget.init();
        invalidate();

        return cell;
    }

    @Override
    public void clear() {
        view.clear();

        cells.removeIf(cell -> cell.widget() != header && cell.widget() != view);
    }

    @Override
    protected void onCalculateWidgetPositions() {
        super.onCalculateWidgetPositions();

        if (contentOffsetY != 0) {
            for (Cell<?> cell : cells) {
                if (cell.widget() != header) cell.move(0, contentOffsetY);
            }

            contentOffsetY = 0;
        }
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        HontunGuiTheme theme = theme();
        Color backgroundColor = ColorUtils.withAlpha(theme.mantleColor(), theme.windowOpacity());

        int shadowOffset = getShadowOffset();
        double windowHeight = Math.max((height - header.height) * animation.getProgress(), 0);

        if (theme.effWindowShadow()) {
            Color shadowColor = theme.shadowColor();

            roundedRect().pos(x - shadowOffset, y - shadowOffset)
                         .size(width + shadowOffset * 2,
                                 header.height + windowHeight + shadowOffset * 2)
                         .radius(radius() + shadowOffset / 2f)
                         .color(shadowColor)
                         .render();
        }

        if (expanded || animation.isRunning())
            roundedRect().pos(x, y + header.height)
                         .size(width, windowHeight)
                         .radius(radius() - shadowOffset, Corners.BOTTOM)
                         .color(backgroundColor)
                         .render();
    }

    @Override
    public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (!visible) return true;

        double progress = animation.getProgress();
        boolean isAnimating = animation.isRunning();
        double contentHeight = height - header.height;

        if (isAnimating) {
            int shadowOffset = getShadowOffset();
            double windowHeight = Math.max(contentHeight * progress, 0);

            if (progress > 1.0) {
                double overshot = Math.max(progress - 1, 0) * contentHeight;
                double shift = overshot / 2;

                if (contentOffsetY != shift) {
                    contentOffsetY = shift;
                    invalidate();
                }
            }

            renderer.scissorStart(
                    x - shadowOffset,
                    y - shadowOffset,
                    width + shadowOffset * 2,
                    header.height + windowHeight + shadowOffset * 2
            );
        }

        boolean toReturn = super.render(renderer, mouseX, mouseY, delta);

        if (isAnimating) renderer.scissorEnd();

        return toReturn;
    }

    @Override
    protected void renderWidget(WWidget widget, GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (expanded || animation.isRunning() || widget instanceof WHeader)
            widget.render(renderer, mouseX, mouseY, delta);
    }

    @Override
    protected boolean propagateEvents(WWidget widget) {
        return widget instanceof WHeader || expanded;
    }

    @Override
    public void setExpanded(boolean expanded) {
        super.setExpanded(expanded);

        if (animation != null)
            animation.reverse();

        if (expanded && cornerAnimation != null)
            cornerAnimation.finishedAt(Direction.FORWARDS);
    }

    @Override
    protected WHeader header(WWidget icon) {
        return new WHontunHeader(icon);
    }

    private class WHontunHeader extends WHeader {
        private WHorizontalList list;
        private WTriangle openIndicator;

        public WHontunHeader(WWidget icon) {
            super(icon);
        }

        @Override
        public void init() {
            list = add(theme.horizontalList())
                    .padHorizontal(theme.scale(10))
                    .padVertical(theme.scale(8))
                    .expandX()
                    .widget();

            list.spacing = theme().scale(6);

            if (icon != null)
                add(icon).centerY();

            if (beforeHeaderInit != null)
                beforeHeaderInit.accept(this);

            add(theme.label(title, true)).expandX().centerY();

            openIndicator = add(theme().triangle())
                    .right()
                    .centerY()
                    .widget();
        }

        @Override
        public <T extends WWidget> Cell<T> add(T widget) {
            if (list != null) return list.add(widget);
            return super.add(widget);
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            HontunGuiTheme theme = theme();
            double cornerProgress = cornerAnimation.getProgress();

            if (!expanded
                    && animation.getProgress() <= 0.0
                    && !cornerAnimation.isRunning()
                    && cornerProgress > 0) {
                cornerAnimation.start(Direction.BACKWARDS);
            }

            roundedRect().bounds(this)
                         .radii(radius(),
                                radius(),
                                (float) (radius() * (1 - cornerProgress)),
                                (float) (radius() * (1 - cornerProgress)))
                         .color(theme.crustColor())
                         .render();

            if (expanded || (animation.isRunning() && !cornerAnimation.isRunning())) {
                Color transparentColor = ColorUtils.withAlpha(theme.baseColor(), 0);

                Color semiTransparentColor = ColorUtils.withAlpha(
                        theme.baseColor(),
                        0.5 * theme.windowOpacity()
                );

                renderer.quad(
                        x,
                        y + height,
                        width,
                        12,
                        semiTransparentColor,
                        semiTransparentColor,
                        transparentColor,
                        transparentColor
                );
            }

            openIndicator.rotation = 90 + 90 * animation.getProgress();
        }

        @Override
        public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            boolean render = super.render(renderer, mouseX, mouseY, delta);
            animProgress = 1;
            return render;
        }

        @Override
        public boolean onMouseClicked(MouseButtonEvent click, boolean used) {
            boolean clicked = super.onMouseClicked(

                    click,

                    used
            );

            if (clicked && shouldSnap) {
                mouseOffsetX = click.x() - x;
                mouseOffsetY = click.y() - y;
            }

            return clicked;
        }

        @Override
        public boolean mouseReleased(MouseButtonEvent click) {
            if (shouldSnap) modulesScreen.showGrid(false);
            return super.mouseReleased(

                    click

            );
        }

        @Override
        public void onMouseMoved(double mouseX, double mouseY, double lastMouseX, double lastMouseY) {
            if (!dragging) return;

            double deltaX = shouldSnap ? snapToGrid(mouseX - mouseOffsetX) - x : mouseX - lastMouseX;
            double deltaY = shouldSnap ? snapToGrid(mouseY - mouseOffsetY) - y : mouseY - lastMouseY;

            WHontunWindow.this.move(deltaX, deltaY);

            moved = true;
            movedX = x;
            movedY = y;

            if (id != null) {
                WindowConfig config = theme.getWindowConfig(id);

                config.x = x;
                config.y = y;
            }

            if (shouldSnap && !modulesScreen.showGrid()) modulesScreen.showGrid(true);
            dragged = true;
        }
    }

    private double snapToGrid(double value) {
        return Math.round(value / gridSize) * gridSize;
    }

    private int getShadowOffset() {
        return theme().effWindowShadow() ? SHADOW_OFFSET : 0;
    }
}
