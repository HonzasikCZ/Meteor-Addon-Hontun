package cz.honzasik.hontun.gui.theme.widgets.pressable;

import cz.honzasik.hontun.gui.api.animation.Animation;
import cz.honzasik.hontun.gui.api.animation.Direction;
import cz.honzasik.hontun.gui.api.animation.Easing;
import cz.honzasik.hontun.gui.theme.HontunGuiTheme;
import cz.honzasik.hontun.gui.theme.HontunWidget;
import cz.honzasik.hontun.gui.theme.icons.HontunBuiltinIcons;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;

public class WHontunCheckbox extends WCheckbox implements HontunWidget {
    private Animation animation;

    public WHontunCheckbox(boolean checked) {
        super(checked);
    }

    @Override
    public void init() {
        super.init();

        animation = new Animation(
                Easing.BACK_IN_OUT,
                300,
                checked ? Direction.FORWARDS : Direction.BACKWARDS
        );
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (!checked || animation.isRunning()) background(false, mouseOver).render();

        if (!checked && animation.isFinished()) return;

        renderCheckmark(renderer);
    }

    @Override
    protected void onCalculateSize() {
        super.onCalculateSize();
        height *= 0.9;
        width *= 0.9;
    }

    @Override
    protected void onPressed(int button) {
        super.onPressed(button);
        animation.start(checked ? Direction.FORWARDS : Direction.BACKWARDS);
    }

    private void renderCheckmark(GuiRenderer renderer) {
        HontunGuiTheme theme = theme();
        double progress = animation.getProgress();
        double size = width * progress;
        double tickSize = size * 0.6;
        double minSize = theme.scale(6);

        if (size <= minSize) return;

        double centerOffset = (width - size) / 2;

        roundedRect().pos(x + centerOffset, y + centerOffset)
                     .size(size, size)
                     .radius(smallRadius())
                     .color(theme.accentColor())
                     .outline(theme.accentColor().copy().a(mouseOver ? 140 : 80), 3f)
                     .render();

        if (tickSize <= minSize) return;

        centerOffset = (width - tickSize) / 2;

        renderer.rotatedQuad(
                x + centerOffset,
                y + centerOffset,
                tickSize,
                tickSize,
                0,
                HontunBuiltinIcons.TICK.texture(),
                theme.backgroundColor.get(160)
        );
    }

    public void setChecked(boolean checked) {
        if (this.checked == checked) return;
        this.checked = checked;
        animation.start(checked ? Direction.FORWARDS : Direction.BACKWARDS);
    }
}
